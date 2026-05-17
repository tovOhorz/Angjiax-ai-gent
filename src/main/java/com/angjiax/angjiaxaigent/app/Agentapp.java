package com.angjiax.angjiaxaigent.app;

import com.angjiax.angjiaxaigent.advisor.MyLoggerAdvisor;
import com.angjiax.angjiaxaigent.advisor.ReReadingAdvisor;
import com.angjiax.angjiaxaigent.chatmemory.FileBasedChatMemory;
import com.angjiax.angjiaxaigent.demo.rag.QueryRewriter;
import com.angjiax.angjiaxaigent.rag.AgentAppRagCustomAdvisorFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
@Slf4j
public class Agentapp {

    @Resource
    private VectorStore agentAppVectorStore;

    @Resource
    private QueryRewriter queryRewriter;

//    @Resource 需要创建云数据库
//    private Advisor agentappRagCloudAdvisor;

//    @Resource
//    private VectorStore pgVectorVectorStore;

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "扮演深耕电商领域的优惠导购专家。开场向用户表明身份，告知用户可咨询各类商品优惠及平台活动规则。" +
            "引导用户明确购物需求：询问目标商品类别（如数码、美妆、服饰）、预算范围及使用场景。" +
            "基于电商平台现有优惠策略（如跨店满减、会员券、百亿补贴），结合用户描述的需求，分析最划算的领券与凑单方案。" +
            "引导用户提供商品链接、当前售价或心仪款式，以便RAG系统精准检索叠加优惠，生成专属省钱攻略。";

    public Agentapp(ChatModel dashscopeChatModel) {
        // 初始化基于文件的对话记忆
        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory)
                )
                .build();
    }

    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .stream()
                .content();
    }


    record AgentReport(String title, List<String> suggestions) {
    }

    public AgentReport doChatWithReport(String message, String chatId) {
        AgentReport agentReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成提问结果，标题为{用户名}的提问报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(AgentReport.class);
        log.info("AgentReport: {}", agentReport);
        return agentReport;
    }

    /*
    * 和RAG知识库进行对话7
    * */
    public String doChatWithRag(String message, String chatId) {
        String rewrittenMessage = queryRewriter.doQueryRewrite(message);
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(rewrittenMessage)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                //开启日志,便于观察结果
                .advisors(new MyLoggerAdvisor())
                //应用RAG知识库问答
                .advisors(new QuestionAnswerAdvisor(agentAppVectorStore))
                //应用RAG检索增强服务 本项目需要创建云数据库
                //.advisors(agentappRagCloudAdvisor)
                //应用RAG检索增强服务 基于PgVector向量存储
                //.advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
//                .advisors(
//                        AgentAppRagCustomAdvisorFactory.createAgentAppRagCustomAdvisor(agentAppVectorStore,"高端,苹果,5G,旗舰")
//                )
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        return content;
    }

    /**
     * 和RAG知识库进行对话 - 流式返回
     */
    public Flux<String> doChatWithRagByStream(String message, String chatId) {
        String rewrittenMessage = queryRewriter.doQueryRewrite(message);
        return chatClient
                .prompt()
                .user(rewrittenMessage)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                //开启日志,便于观察结果
                .advisors(new MyLoggerAdvisor())
                //应用RAG知识库问答
                .advisors(new QuestionAnswerAdvisor(agentAppVectorStore))
                .stream()
                .content();
    }

    @Resource
    private ToolCallback[] allTools;


    public String doChatWithTools(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                .tools(allTools)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    @Resource
    private ToolCallbackProvider toolCallbackProvider;
    /*
        调用Mcp服务
     */
    public String doChatWithMcp(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                .tools(toolCallbackProvider)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }




}
