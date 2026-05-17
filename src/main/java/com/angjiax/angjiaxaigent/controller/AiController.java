package com.angjiax.angjiaxaigent.controller;

import com.angjiax.angjiaxaigent.agent.AngjiaxManus;
import com.angjiax.angjiaxaigent.app.Agentapp;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private Agentapp agentapp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    @GetMapping("/agent_app/chat/sync")
    public String doChatWithAgentAppSync(String message, String chatId){
        return agentapp.doChat(message,chatId);
    }

    @GetMapping(value = "/agent_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithAgentAppSSE(String message, String chatId) {
        return agentapp.doChatByStream(message, chatId);
    }

    @GetMapping(value = "/agent_app/chat/server_sent_event")
    public Flux<ServerSentEvent<String>> doChatWithAgentAppServerSentEvent(String message, String chatId) {
        return agentapp.doChatByStream(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    @GetMapping("/agent_app/chat/sse/emitter")
    public SseEmitter doChatWithAgentAppSseEmitter(String message, String chatId) {
        // 创建一个超时时间较长的 SseEmitter
        SseEmitter emitter = new SseEmitter(180000L); // 3分钟超时
        // 获取 Flux 数据流并直接订阅
        agentapp.doChatByStream(message, chatId)
                .subscribe(
                        // 处理每条消息
                        chunk -> {
                            try {
                                emitter.send(chunk);
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        // 处理错误
                        emitter::completeWithError,
                        // 处理完成
                        emitter::complete
                );
        // 返回emitter
        return emitter;
    }

    /**
     * 流式调用 Manus 超级智能体
     *
     * @param message
     * @return
     */
    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        AngjiaxManus angjiaxManus = new AngjiaxManus(allTools, dashscopeChatModel);
        return angjiaxManus.runStream(message);
    }

    /**
     * RAG 知识库问答 - 流式 SSE 调用/
     */
    @GetMapping(value = "/agent_app/chat/rag/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithRagSSE(String message, String chatId) {
        return agentapp.doChatWithRagByStream(message, chatId);
    }

    /**
     * RAG 知识库问答 - ServerSentEvent 格式
     */
    @GetMapping(value = "/agent_app/chat/rag/server_sent_event")
    public Flux<ServerSentEvent<String>> doChatWithRagServerSentEvent(String message, String chatId) {
        return agentapp.doChatWithRagByStream(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    /**
     * RAG 知识库问答 - SseEmitter 方式
     */
    @GetMapping("/agent_app/chat/rag/sse/emitter")
    public SseEmitter doChatWithRagSseEmitter(String message, String chatId) {
        // 创建一个超时时间较长的 SseEmitter
        SseEmitter emitter = new SseEmitter(180000L); // 3分钟超时
        // 获取 Flux 数据流并直接订阅
        agentapp.doChatWithRagByStream(message, chatId)
                .subscribe(
                        // 处理每条消息
                        chunk -> {
                            try {
                                emitter.send(chunk);
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        // 处理错误
                        emitter::completeWithError,
                        // 处理完成
                        emitter::complete
                );
        // 返回emitter
        return emitter;
    }





}
