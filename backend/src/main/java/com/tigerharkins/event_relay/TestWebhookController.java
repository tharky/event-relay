package com.tigerharkins.event_relay;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestWebhookController {

    @PostMapping("/webhook")
    public Map<String, String> receiveWebhook(
            @RequestBody Map<String, Object> payload) {

        System.out.println("Received webhook: " + payload);

        return Map.of("status", "received");
    }
}