package com.tigerharkins.event_relay;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final WebhookDeliveryService deliveryService;
    private final EventStore eventStore;

    public EventController(
            WebhookDeliveryService deliveryService,
            EventStore eventStore) {

        this.deliveryService = deliveryService;
        this.eventStore = eventStore;
    }

    @PostMapping
    public Map<String, String> createEvent(
            @RequestBody EventRequest request) {

        String eventId = UUID.randomUUID().toString();

        EventStatus eventStatus = new EventStatus(eventId);

        eventStore.save(eventStatus);

        deliveryService.deliver(eventId, request);

        return Map.of(
                "eventId", eventId,
                "status", "queued"
        );
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventStatus> getEvent(
            @PathVariable String eventId) {

        EventStatus status = eventStore.get(eventId);

        if (status == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(status);
    }
}