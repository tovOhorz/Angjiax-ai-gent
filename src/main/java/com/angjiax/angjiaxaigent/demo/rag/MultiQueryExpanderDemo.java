package com.angjiax.angjiaxaigent.demo.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.expansion.QueryExpander;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MultiQueryExpanderDemo {
    private ChatClient.Builder chatClientBuilder;

    public MultiQueryExpanderDemo(ChatModel dashscopeChatModel){
        this.chatClientBuilder = ChatClient.builder(dashscopeChatModel);
    }

    public List<Query> expand(String query){
        MultiQueryExpander queryExpander = MultiQueryExpander.builder()
                .chatClientBuilder(chatClientBuilder)
                .numberOfQueries(3)
                .build();
        Query originalQuery = new Query(query);
        List<Query> queries = queryExpander.expand(originalQuery);
        return queries;
    }

}
//@Component
//public class MultiQueryExpanderDemo {
//    private final QueryExpander queryExpander;
//
//    public MultiQueryExpanderDemo(ChatModel dashscopeChatModel) {
//        ChatClient.Builder builder = ChatClient.builder(dashscopeChatModel);
//        queryExpander = MultiQueryExpander.builder()
//                .chatClientBuilder(builder)
//                .numberOfQueries(3)
//                .build();
//    }
//
//    public List<Query> expand(String query) {
//        Query originalQuery = new Query(query);
//        return queryExpander.expand(originalQuery);
//    }
//}
