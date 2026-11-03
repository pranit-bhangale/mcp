package com.mcp.server.tools;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class DateTimeTool {

    @McpTool(
            name = "get_current_date",
            description = "Get the current system date."
    )
    public String getCurrentDate() {
        return LocalDate.now().toString();
    }

    @McpTool(
            name = "get_current_time",
            description = "Get the current system time."
    )
    public String getCurrentTime() {
        return LocalTime.now()
                .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    @McpTool(
            name = "get_current_datetime",
            description = "Get the current system date and time."
    )
    public String getCurrentDateTime() {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @McpTool(
            name = "get_system_timezone",
            description = "Get the current system timezone."
    )
    public String getSystemTimezone() {
        return ZoneId.systemDefault().toString();
    }
}
