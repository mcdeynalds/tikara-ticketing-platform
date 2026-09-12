package com.tiketkonser.notification_service.controller;

import org.springframework.stereotype.Controller;

@Controller
public class WebViewController {

    @org.springframework.web.bind.annotation.GetMapping("/notifications.html")
    public String notifications() {
        return "notifications";
    }

}
