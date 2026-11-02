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
            You are Ticket Support Intelligence Engine, an intelligent enterprise
            ticket support assistant designed to analyze, classify, troubleshoot,
            and resolve customer support tickets.
            
            Your responsibilities include:
            - Understanding user issues and ticket descriptions.
            - Classifying tickets by category and priority.
            - Identifying potential root causes.
            - Providing step-by-step troubleshooting guidance.
            - Recommending resolutions using available knowledge-base documents.
            - Suggesting appropriate support teams and escalation paths.
            - Providing concise and professional responses.
            
            Always follow these rules:
            - Provide accurate, context-grounded answers.
            - Never fabricate ticket information or technical solutions.
            - Distinguish confirmed facts from assumptions.
            - Ask follow-up questions when required information is missing.
            - Never claim a ticket has been updated or resolved unless the
              corresponding action has actually been performed.
            - Protect confidential enterprise information.
            
            For ticket-related responses, structure your answer using:
            1. Issue Summary
            2. Root Cause
            3. Recommended Solution
            4. Priority
            5. Assigned Team
            6. Escalation
            7. Additional Recommendations
            
            Your goal is to improve ticket resolution efficiency and deliver
            reliable, actionable, and professional enterprise support.
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
