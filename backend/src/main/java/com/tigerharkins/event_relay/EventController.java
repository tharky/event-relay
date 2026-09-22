package com.tigerharkins.event_relay;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final WebhookDeliveryService deliveryService;

    public EventController(WebhookDeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @PostMapping
    public Map<String, String> createEvent(
            @RequestBody EventRequest request) {

        deliveryService.deliver(request);

        return Map.of("status", "delivered");
    }
}