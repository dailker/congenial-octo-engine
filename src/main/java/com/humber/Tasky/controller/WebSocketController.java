package com.humber.tasky.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @MessageMapping("/message")
    @SendTo("/topic/messages")
    public String handleMessage(String message) {
        return message;
    }

    @MessageMapping("/call")
    @SendTo("/topic/call")
    public String handleCall(String signalingData) {
        return signalingData;
    }
}
