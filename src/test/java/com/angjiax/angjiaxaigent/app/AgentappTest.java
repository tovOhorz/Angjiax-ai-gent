package com.angjiax.angjiaxaigent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
class AgentappTest {

    @Resource
    private Agentapp agentapp;

    @Test
    void testChat() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我想买一件白衬衫。";
        String answer = agentapp.doChat(message, chatId);
        Assertions.assertNotNull(answer);

    }


    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是大白，我想买一件黄大衣";
        Agentapp.AgentReport agentReport = agentapp.doChatWithReport(message,chatId);
        Assertions.assertNotNull(agentReport);
    }

    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "你好，我是大白，我想买一台手机";
        String answer = agentapp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithMcp(){
        String chatId = UUID.randomUUID().toString();
        //测试地图Mcp
//        String message = "你好，帮我推荐一下长沙五一广场著名景点，并且返回一张景点照片。";
//        String answer = agentapp.doChatWithMcp(message, chatId);
//        Assertions.assertNotNull(answer);
        // 测试图片搜索 MCP
        String message = "帮我搜索一些长沙橘子洲的图片";
        String answer =  agentapp.doChatWithMcp(message, chatId);
        Assertions.assertNotNull(answer);
    }




}
