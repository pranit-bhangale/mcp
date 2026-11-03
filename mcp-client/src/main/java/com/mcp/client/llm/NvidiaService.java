package com.mcp.client.llm;

import com.mcp.client.chat.advisor.TokenLoggingAdvisor;
import com.mcp.client.chat.stream.ChatEventStream;
import com.mcp.client.tools.CalculatorTools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NvidiaService implements AiService {

    private final ChatClient chatClient;
    private final JsonMapper jsonMapper;
    private final CalculatorTools calculatorTools;

    @Override
    public Flux<ServerSentEvent<String>> askAiAgent(final String prompt) {
        final ChatEventStream events = new ChatEventStream(this.jsonMapper);
        final Flux<ServerSentEvent<String>> answerTokens = this.chatClient.prompt()
                .advisors(events.observer())
                .user(prompt)
                .advisors(a -> a.param(TokenLoggingAdvisor.TRACE_ID_KEY, UUID.randomUUID().toString()))
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, UUID.randomUUID().toString()))
                .tools(this.calculatorTools)
                .stream()
                .content()
                .map(events::token);
        return events.mergeWith(answerTokens);
    }
}
