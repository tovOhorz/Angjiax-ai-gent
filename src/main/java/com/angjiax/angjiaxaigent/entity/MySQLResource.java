package com.angjiax.angjiaxaigent.entity;

import lombok.Data;

import java.util.List;

@Data
public class MySQLResource {

	// MySQL connection properties
	private final String host;
	private final int port;
	private final String database;
	private final String username;
	private final String password;

	// Query settings
	private final String query;
	private final List<String> contentColumns;
	private final List<String> metadataColumns;

	// Default values
	public static final String DEFAULT_HOST = "127.0.0.1";
	public static final int DEFAULT_PORT = 3306;

	public MySQLResource(String host, int port, String database, String username, String password,
						 String query, List<String> contentColumns, List<String> metadataColumns) {
		this.host = host;
		this.port = port;
		this.database = database;
		this.username = username;
		this.password = password;
		this.query = query;
		this.contentColumns = contentColumns;
		this.metadataColumns = metadataColumns;
	}

	public MySQLResource(String database, String username, String password, String query,
						 List<String> contentColumns, List<String> metadataColumns) {
		this(DEFAULT_HOST, DEFAULT_PORT, database, username, password, query, contentColumns, metadataColumns);
	}

	public MySQLResource(String database, String query, List<String> contentColumns, List<String> metadataColumns) {
		this(DEFAULT_HOST, DEFAULT_PORT, database, "root", "root", query, contentColumns, metadataColumns);
	}
}