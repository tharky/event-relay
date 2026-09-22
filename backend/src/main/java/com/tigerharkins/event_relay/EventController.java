package com.tigerharkins.event_relay;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final WebhookDeliveryService deliveryService;
    private final EventRepository eventRepository;
    private final DeliveryAttemptRepository attemptRepository;
    private final JsonMapper jsonMapper;

    public EventController(
            WebhookDeliveryService deliveryService,
            EventRepository eventRepository,
            DeliveryAttemptRepository attemptRepository,
            JsonMapper jsonMapper) {

        this.deliveryService = deliveryService;
        this.eventRepository = eventRepository;
        this.attemptRepository = attemptRepository;
        this.jsonMapper = jsonMapper;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createEvent(
            @RequestBody EventRequest request)
            throws JacksonException {

        String eventId = UUID.randomUUID().toString();

        String payloadJson =
                jsonMapper.writeValueAsString(request.data());

        EventEntity event = new EventEntity(
                eventId,
                request.type(),
                request.destination(),
                payloadJson
        );

        eventRepository.save(event);

        deliveryService.deliver(eventId, request);

        return ResponseEntity.accepted().body(
                Map.of(
                        "eventId", eventId,
                        "status", "queued"
                )
        );
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventEntity> getEvent(
            @PathVariable String eventId) {

        return eventRepository
                .findById(eventId)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build());
    }

    @GetMapping("/{eventId}/attempts")
    public List<DeliveryAttemptEntity> getAttempts(
            @PathVariable String eventId) {

        return attemptRepository
                .findByEventIdOrderByAttemptNumberAsc(eventId);
    }
}