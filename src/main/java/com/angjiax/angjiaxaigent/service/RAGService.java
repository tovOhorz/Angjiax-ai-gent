package com.angjiax.angjiaxaigent.service;

import com.angjiax.angjiaxaigent.entity.MySQLResource;
import com.angjiax.angjiaxaigent.rag.MySQLDocumentReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class RAGService {

    @Autowired
    private MySQLDocumentReader mySQLDocumentReader;

    /*
     * 加载所有表的数据
     * */
    public List<Document> loadAllTablesDate(){
        List<Document> allDocuments = new ArrayList<>();
        // 1. 商品表
        allDocuments.addAll(loadProducts());
        log.info("总共加载 {} 个文档", allDocuments.size());
        return allDocuments;
    }

    /**
     * 加载商品表
     */
    private List<Document> loadProducts() {
        MySQLResource resource = new MySQLResource(
                "agent",  // 数据库名
                // 修改 SQL，使用实际的字段名
                "SELECT id, productName, categoryName, brand, originalPrice, currentPrice, stock, salesVolume, rating, platform, productDesc, status FROM product WHERE status = 1",
                Arrays.asList("productName", "productDesc", "categoryName", "brand", "platform", "rating"),  // 内容列：商品名、描述、分类、品牌、平台、评分
                Arrays.asList("id", "productName", "categoryName", "brand", "originalPrice", "currentPrice", "stock", "salesVolume", "rating", "platform", "platformUrl", "imageUrl")  // 元数据列
        );

        mySQLDocumentReader.setMySQLResource(resource);
        List<Document> docs = mySQLDocumentReader.get();
        log.info("商品表加载 {} 条", docs.size());

        return docs;
    }
}
//@Service
//@Slf4j
//public class RAGService {
//
//    @Autowired
//    private MySQLDocumentReader mySQLDocumentReader;
//
//    /*
//    * 加载所有表的数据
//    * */
//    public List<Document> loadAllTablesDate(){
//        List<Document> allDocuments = new ArrayList<>();
//        // 1. 用户表
//        allDocuments.addAll(loadProducts());
//        log.info("总共加载 {} 个文档", allDocuments.size());
//        return allDocuments;
//    }
//
//    /**
//     * 加载商品表
//     */
//    private List<Document> loadProducts() {
//        MySQLResource resource = new MySQLResource(
//                "agent",  // 数据库名
//                "SELECT id, product_code, name, category, sub_category, brand, price, discount_price, description, tags FROM product WHERE status = 1",
//                Arrays.asList("name", "description", "category", "brand", "price"),  // 内容列：商品名、描述、分类、品牌、价格
//                Arrays.asList("id", "product_code", "category", "brand", "tags", "discount_price")  // 元数据列
//        );
//
//        mySQLDocumentReader.setMySQLResource(resource);
//        List<Document> docs = mySQLDocumentReader.get();
//        log.info("商品表加载 {} 条", docs.size());
//
//        return docs;
//    }
//
//}