package com.tiketkonser.admin_service.controller;

import org.springframework.stereotype.Controller;

@Controller
public class WebViewController {

    @org.springframework.web.bind.annotation.GetMapping("/admin-dashboard.html")
    public String admin_dashboard() {
        return "admin-dashboard";
    }

}
