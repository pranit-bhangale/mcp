package com.ticket.support.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the AI Agent SSE viewer as a Thymeleaf-rendered page.
 * <p>
 * Open {@code http://localhost:8080/api/agent/viewer} in a browser and use the
 * form to stream tokens from {@code POST /api/agent/chat}.
 */
@Controller
public class ViewerController {

    @GetMapping("/api/agent/viewer")
    public String viewer(Model model) {
        model.addAttribute("version", "v1");
        return "agent/viewer";
    }
}
