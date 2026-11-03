package com.mcp.client.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the AI Agent chatbot UI as a Thymeleaf-rendered page.
 * <p>
 * Open {@code http://localhost:8080/api/agent/chatbot} in a browser to chat
 * with the AI agent, watch tool calls animate, and stream responses.
 */
@Controller
public class ChatbotController {

    @GetMapping(value = "/api/agent/chatbot")
    public String chatbot() {
        return "chatbot/chatbot";
    }
}
