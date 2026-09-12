package com.tiketkonser.queue_service.controller;

import org.springframework.stereotype.Controller;

@Controller
public class WebViewController {

    @org.springframework.web.bind.annotation.GetMapping("/waiting-room.html")
    public String waiting_room() {
        return "waiting-room";
    }

}
