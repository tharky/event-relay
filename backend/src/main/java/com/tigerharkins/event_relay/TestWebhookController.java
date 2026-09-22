package com.tigerharkins.event_relay;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestWebhookController {

    private final Map<String, Integer> receivedCounts =
            new ConcurrentHashMap<>();

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> receiveWebhook(
            @RequestBody Map<String, Object> payload,
            @RequestParam(defaultValue = "0") int delayMs,
            @RequestParam(defaultValue = "200") int status,
            @RequestParam(defaultValue = "0") int failuresBeforeSuccess,
            @RequestParam(defaultValue = "500") int failureStatus)
            throws InterruptedException {

        String eventId =
                String.valueOf(
                        payload.getOrDefault(
                                "eventId",
                                "manual"
                        )
                );

        int attemptNumber =
                receivedCounts.merge(
                        eventId,
                        1,
                        Integer::sum
                );

        System.out.println(
                "Received webhook attempt "
                        + attemptNumber
                        + ": "
                        + payload
        );

        if (delayMs > 0) {
            Thread.sleep(delayMs);
        }

        int responseStatus;

        if (attemptNumber <= failuresBeforeSuccess) {
            responseStatus = failureStatus;
        } else {
            responseStatus = status;
        }

        return ResponseEntity
                .status(responseStatus)
                .body(Map.of(
                        "status", "received",
                        "delayMs", delayMs,
                        "attempt", attemptNumber,
                        "responseStatus", responseStatus
                ));
    }
}