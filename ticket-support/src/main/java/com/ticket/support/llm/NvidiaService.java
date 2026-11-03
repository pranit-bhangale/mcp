package com.ticket.support.llm;

import com.ticket.support.chat.stream.ChatEventStream;
import com.ticket.support.tools.TicketTool;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
public class NvidiaService implements AiService {

    private final ChatClient chatClient;
    private final TicketTool ticketTool;
    private final JsonMapper jsonMapper;

    @Override
    public Flux<ServerSentEvent<String>> askAiAgent(final String prompt, final String conversationId) {
        final ChatEventStream events = new ChatEventStream(this.jsonMapper);
        final Flux<ServerSentEvent<String>> answerTokens = this.chatClient.prompt()
                .advisors(events.observer())
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(prompt)
                .tools(this.ticketTool)
                .stream()
                .content()
                .map(events::token);
        return events.mergeWith(answerTokens);
    }
}
