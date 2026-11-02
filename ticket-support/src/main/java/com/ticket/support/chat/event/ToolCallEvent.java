package com.ticket.support.chat.event;

/**
 * A tool lifecycle event observed inside the tool-calling loop.
 * Serialized to the browser as the SSE "tool" event.
 */
public record ToolCallEvent(String id, String name, Phase phase) {

    public enum Phase {
        STARTED, COMPLETED
    }
}