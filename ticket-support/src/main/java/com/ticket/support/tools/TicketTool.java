package com.ticket.support.tools;

import com.ticket.support.dto.TicketRequest;
import com.ticket.support.dto.TicketResponse;
import com.ticket.support.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketTool {

    private final TicketService ticketService;

    @Tool(
            name = "create_ticket",
            description = """
                    Creates a new customer support ticket.
                    
                    Use this tool when the user:
                    - Reports a technical or banking issue.
                    - Requests help with a problem.
                    - Wants to raise or register a new complaint.
                    - Explicitly asks to create a support ticket.
                    
                    Before creating a ticket, check whether an existing
                    ticket already covers the same issue.
                    
                    Collect the required ticket information before calling
                    this tool. Do not invent user details or issue information.
                    """
    )
    public TicketResponse createTicket(
            @ToolParam(description = """
                    Contains the information required to create a support ticket:
                    userId, username, summary, description, priority, and category.
                    """)
            TicketRequest request
    ) {
        return ticketService.createTicket(request);
    }

    @Tool(
            name = "get_ticket_detail_by_id",
            description = """
                    Retrieves complete information about an existing support ticket.
                    
                    Use this tool when the user:
                    - Wants to check an existing ticket.
                    - Asks about ticket status or progress.
                    - Wants to view ticket priority or category.
                    - Wants to track a complaint.
                    - Requests details about a specific ticket.
                    
                    Requires a valid ticket ID, such as TKT-483921.
                    Do not call this tool without a ticket ID.
                    """
    )
    public TicketResponse getTicketDetailById(
            @ToolParam(description = """
                    Unique support ticket identifier.
                    Example: TKT-483921.
                    """)
            String ticketId
    ) {
        return ticketService.fetchTicketDetailById(ticketId);
    }
}
