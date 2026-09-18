package com.tiketkonser.checkin_service.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebViewController {

    @GetMapping({"/", "/scan-gate"})
    public String scanGate() {
        return "forward:/scan-gate.html";
    }

}
