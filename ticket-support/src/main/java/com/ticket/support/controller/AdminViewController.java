package com.ticket.support.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminViewController {

    @GetMapping(value = {"/admin", "/admin/tickets"}, version = "v1")
    public String adminPage() {
        return "admin/tickets";
    }
}
