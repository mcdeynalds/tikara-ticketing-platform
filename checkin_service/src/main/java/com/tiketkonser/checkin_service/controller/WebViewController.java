package com.tiketkonser.checkin_service.controller;

import org.springframework.stereotype.Controller;

@Controller
public class WebViewController {

    @org.springframework.web.bind.annotation.GetMapping("/scan-gate.html")
    public String scan_gate() {
        return "scan-gate";
    }

}
