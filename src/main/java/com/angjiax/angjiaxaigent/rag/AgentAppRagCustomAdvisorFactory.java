package com.angjiax.angjiaxaigent.rag;

import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.document.DocumentTransformer;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

public class AgentAppRagCustomAdvisorFactory {


    public static Advisor createAgentAppRagCustomAdvisor(VectorStore vectorStore, String tags){
        //过滤特定状态的文档
        Filter.Expression expression = new FilterExpressionBuilder()
                .eq("tags", tags)
                .build();
        DocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .filterExpression(expression)
                .similarityThreshold(0.5)//相似度阈值
                .topK(3)//返回文档数量
                .build();
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .queryAugmenter(AgentAppContextualQueryAugmenterFactory.createInstance())
                .build();
    }

}
