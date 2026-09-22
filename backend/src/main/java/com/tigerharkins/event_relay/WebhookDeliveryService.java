package com.tigerharkins.event_relay;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class WebhookDeliveryService {

    private final RestClient restClient = RestClient.create();
    private final EventStore eventStore;

    public WebhookDeliveryService(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    @Async
    public void deliver(String eventId, EventRequest event) {

        EventStatus status = eventStore.get(eventId);

        try {
            status.setStatus("delivering");
            status.incrementAttempts();

            Map<String, Object> payload = Map.of(
                    "type", event.type(),
                    "data", event.data()
            );

            restClient.post()
                    .uri(event.destination())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            status.setStatus("delivered");

        } catch (Exception e) {

            status.setStatus("failed");

            System.err.println(
                    "Delivery failed for event " + eventId + ": " + e.getMessage()
            );
        }
    }
}