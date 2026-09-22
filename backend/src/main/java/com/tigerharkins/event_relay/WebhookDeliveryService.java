package com.tigerharkins.event_relay;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class WebhookDeliveryService {

    private final RestClient restClient = RestClient.create();

    public void deliver(EventRequest event) {

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
    }
}