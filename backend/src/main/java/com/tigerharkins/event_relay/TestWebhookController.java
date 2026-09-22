package com.tigerharkins.event_relay;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestWebhookController {

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> receiveWebhook(
            @RequestBody Map<String, Object> payload,
            @RequestParam(defaultValue = "0") int delayMs,
            @RequestParam(defaultValue = "200") int status) throws InterruptedException {

        System.out.println("Received webhook: " + payload);

        if (delayMs > 0) {
            Thread.sleep(delayMs);
        }

        return ResponseEntity
                .status(status)
                .body(Map.of(
                        "status", "received",
                        "delayMs", delayMs
                ));
    }
}