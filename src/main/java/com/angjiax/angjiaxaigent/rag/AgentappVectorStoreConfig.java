package com.angjiax.angjiaxaigent.rag;

import com.angjiax.angjiaxaigent.service.RAGService;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentTransformer;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AgentappVectorStoreConfig {


    @Resource
    private RAGService ragService;

    @Bean
    VectorStore agentAppVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        List<Document> documents = ragService.loadAllTablesDate();
        //构建向量库
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel).build();

        simpleVectorStore.add(documents);
        return simpleVectorStore;
    }

}
