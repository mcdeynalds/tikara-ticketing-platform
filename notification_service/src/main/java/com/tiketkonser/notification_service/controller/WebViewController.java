package com.tiketkonser.notification_service.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebViewController {

    @GetMapping({"/", "/notifications"})
    public String notifications() {
        return "forward:/notifications.html";
    }

}
