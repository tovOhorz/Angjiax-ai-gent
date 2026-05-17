package com.angjiax.angjiaxaigent.rag;

import com.angjiax.angjiaxaigent.entity.MySQLResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MySQLDocumentReader implements DocumentReader {

	private final JdbcTemplate jdbcTemplate;
	private MySQLResource mysqlResource;

	@Autowired
	public MySQLDocumentReader(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	/**
	 * 设置 MySQL 资源配置
	 */
	public void setMySQLResource(MySQLResource mysqlResource) {
		this.mysqlResource = mysqlResource;
	}

	@Override
	public List<Document> get() {
		if (mysqlResource == null) {
			throw new IllegalStateException("MySQLResource not configured");
		}

		try {
			return executeQueryAndProcessResults();
		} catch (Exception e) {
			throw new RuntimeException("Error executing MySQL query: " + e.getMessage(), e);
		}
	}

	/**
	 * 执行查询并处理结果
	 */
	private List<Document> executeQueryAndProcessResults() {
		return jdbcTemplate.query(mysqlResource.getQuery(), rs -> {
			List<Document> documents = new ArrayList<>();
			ResultSetMetaData metaData = rs.getMetaData();
			List<String> columnNames = getColumnNames(metaData);

			while (rs.next()) {
				Map<String, Object> rowData = extractRowData(rs, columnNames);
				String content = buildContent(rowData);
				Map<String, Object> metadata = buildMetadata(rowData);
				documents.add(new Document(content, metadata));
			}
			return documents;
		});
	}

	/**
	 * 获取列名列表
	 */
	private List<String> getColumnNames(ResultSetMetaData metaData) throws SQLException {
		List<String> columnNames = new ArrayList<>();
		int columnCount = metaData.getColumnCount();
		for (int i = 1; i <= columnCount; i++) {
			columnNames.add(metaData.getColumnName(i));
		}
		return columnNames;
	}

	/**
	 * 提取行数据
	 */
	private Map<String, Object> extractRowData(ResultSet rs, List<String> columnNames) throws SQLException {
		Map<String, Object> rowData = new HashMap<>();
		for (int i = 0; i < columnNames.size(); i++) {
			String columnName = columnNames.get(i);
			Object value = rs.getObject(i + 1);
			rowData.put(columnName, value);
		}
		return rowData;
	}

	/**
	 * 构建文档内容
	 */
	private String buildContent(Map<String, Object> rowData) {
		StringBuilder contentBuilder = new StringBuilder();
		List<String> contentColumns = mysqlResource.getContentColumns();

		if (contentColumns == null || contentColumns.isEmpty()) {
			// 如果未指定内容列，使用所有列
			for (Map.Entry<String, Object> entry : rowData.entrySet()) {
				appendColumnContent(contentBuilder, entry.getKey(), entry.getValue());
			}
		} else {
			// 只使用指定的内容列
			for (String column : contentColumns) {
				if (rowData.containsKey(column)) {
					appendColumnContent(contentBuilder, column, rowData.get(column));
				}
			}
		}
		return contentBuilder.toString().trim();
	}

	/**
	 * 追加列内容
	 */
	private void appendColumnContent(StringBuilder builder, String column, Object value) {
		if (value != null) {
			builder.append(column).append(": ").append(value).append("\n");
		} else {
			builder.append(column).append(": null\n");
		}
	}

	/**
	 * 构建元数据
	 */
	private Map<String, Object> buildMetadata(Map<String, Object> rowData) {
		Map<String, Object> metadata = new HashMap<>();
		metadata.put("source", "mysql");
		metadata.put("database", mysqlResource.getDatabase());
		metadata.put("host", mysqlResource.getHost());

		List<String> metadataColumns = mysqlResource.getMetadataColumns();
		if (metadataColumns != null) {
			for (String column : metadataColumns) {
				if (rowData.containsKey(column)) {
					metadata.put(column, rowData.get(column));
				}
			}
		}
		return metadata;
	}
}