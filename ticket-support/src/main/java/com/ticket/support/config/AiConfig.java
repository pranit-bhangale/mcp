package com.ticket.support.config;

import com.ticket.support.properties.GuardrailProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Slf4j
@Configuration
@EnableConfigurationProperties(GuardrailProperties.class)
public class AiConfig {

    private static final String DEFAULT_SYSTEM_PROMPT = """
            You are a friendly, professional customer support AI assistant.
            
            YOUR RESPONSIBILITIES:
            1. Greet users politely and naturally.
            2. Understand customer support requests.
            3. Help users create new support tickets.
            4. Help users retrieve and track existing tickets.
            5. Answer general customer support questions.
            
            INTENT CLASSIFICATION:
            Identify exactly one intent from:
            - GREETING
            - CREATE_TICKET
            - TRACK_TICKET
            - GENERAL_QUERY
            
            RESPONSE FORMAT:
            Always return a structured response matching this JSON structure:
            
            {
                "intent": "GREETING",
                "message": "Your response to the user",
                "ticket": null
            }
            
            For ticket-related responses, use:
            
            {
                "intent": "CREATE_TICKET",
                "message": "Your response to the user",
                "ticket": {
                    "ticketId": null,
                    "summary": "string",
                    "description": "string",
                    "priority": "MEDIUM",
                    "category": "TECHNICAL",
                    "status": "PENDING"
                }
            }
            
            BEHAVIOR RULES:
            
            GREETING:
            - Respond warmly to greetings.
            - Introduce yourself as a customer support assistant.
            - Do not invoke ticket tools.
            - Set ticket to null.
            
            CREATE_TICKET:
            - Identify the issue reported by the user.
            - Collect missing information when necessary.
            - Check whether an existing ticket already covers the issue.
            - Use the create_ticket tool to create a ticket.
            - Never fabricate ticket IDs or ticket details.
            - Only confirm successful creation after the tool succeeds.
            - Set ticket to null if ticket creation has not succeeded.
            
            TRACK_TICKET:
            - Ask for the ticket ID if it is missing.
            - Use get_ticket_detail_by_id to retrieve ticket details.
            - Never fabricate ticket information.
            - Return the actual ticket details retrieved by the tool.
            
            GENERAL_QUERY:
            - Answer customer questions clearly and politely.
            - Do not create tickets unless requested or appropriate.
            - Set ticket to null.
            
            IMPORTANT:
            - Always return valid structured JSON.
            - Never include Markdown code fences around JSON.
            - Never invent user information.
            - Never claim an operation succeeded without confirmation.
            - If information is insufficient, ask a clear follow-up question.
            """;

    @Bean("chatClient")
    public ChatClient chatClient(ChatClient.Builder builder, List<Advisor> advisors) {
        return builder
                .defaultSystem(DEFAULT_SYSTEM_PROMPT)
                .defaultAdvisors(advisors)
                .build();
    }

    @Bean
    public SafeGuardAdvisor safeGuardAdvisor(GuardrailProperties properties) {
        return new SafeGuardAdvisor(properties.blockedWords());
    }

    @Bean
    public List<Advisor> advisors(SafeGuardAdvisor safeGuardAdvisor, MessageChatMemoryAdvisor memoryAdvisor) {
        return List.of(safeGuardAdvisor, memoryAdvisor);
    }

    @Bean
    public ChatMemory chatMemory(JdbcChatMemoryRepository jdbcChatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .maxMessages(2)
                .build();
    }

    @Bean
    public MessageChatMemoryAdvisor memoryAdvisor(ChatMemory chatMemory) {
        return MessageChatMemoryAdvisor.builder(chatMemory).build();
    }

}
