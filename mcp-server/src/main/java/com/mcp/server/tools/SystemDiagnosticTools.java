package com.mcp.server.tools;

import com.mcp.server.dto.SystemDetail;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.stereotype.Service;

@Service
public class SystemDiagnosticTools {

    @McpTool(
            name = "get_system_details",
            description = "Retrieves basic system information including system name, operating system, version, architecture, and hostname."
    )
    public SystemDetail getSystemDetails() {
        return SystemDetail.builder()
                .systemName(System.getProperty("os.name"))
                .systemType(System.getProperty("os.arch"))
                .systemVersion(System.getProperty("os.version"))
                .manufacturer("Unknown")
                .model("Unknown")
                .hostname("localhost")
                .operatingSystem(System.getProperty("os.name"))
                .osVersion(System.getProperty("os.version"))
                .architecture(System.getProperty("os.arch"))
                .description("Basic system information")
                .build();
    }
}
