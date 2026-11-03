package com.mcp.client.controller;

import com.mcp.client.dto.ChatRequest;
import com.mcp.client.llm.AiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
@Validated
public class AgentController {

    private final AiService aiService;

    @PostMapping(value = "/chat", version = "v1", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chat(@RequestBody @Valid ChatRequest request) {
        return this.aiService.askAiAgent(request.prompt());
    }
}
