package com.mcp.client.llm;

import com.mcp.client.chat.stream.ChatEventStream;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
public class NvidiaService implements AiService {

    private final ChatClient chatClient;
    private final JsonMapper jsonMapper;

    @Override
    public Flux<ServerSentEvent<String>> askAiAgent(final String prompt) {
        final ChatEventStream events = new ChatEventStream(this.jsonMapper);
        final Flux<ServerSentEvent<String>> answerTokens = this.chatClient.prompt()
                .advisors(events.observer())
                .user(prompt)
                .stream()
                .content()
                .map(events::token);
        return events.mergeWith(answerTokens);
    }
}
