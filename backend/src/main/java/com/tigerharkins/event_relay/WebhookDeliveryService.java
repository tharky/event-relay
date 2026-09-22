package com.tigerharkins.event_relay;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class WebhookDeliveryService {

    private final RestClient restClient = RestClient.create();

    private final EventRepository eventRepository;
    private final DeliveryAttemptRepository attemptRepository;

    public WebhookDeliveryService(
            EventRepository eventRepository,
            DeliveryAttemptRepository attemptRepository) {

        this.eventRepository = eventRepository;
        this.attemptRepository = attemptRepository;
    }

    @Async
    public void deliver(String eventId, EventRequest request) {

        EventEntity event = eventRepository
                .findById(eventId)
                .orElseThrow();

        event.setStatus("delivering");
        event.incrementAttempts();
        eventRepository.save(event);

        DeliveryAttemptEntity attempt =
                new DeliveryAttemptEntity(
                        eventId,
                        event.getAttempts()
                );

        attemptRepository.save(attempt);

        try {

            Map<String, Object> payload = Map.of(
                    "type", request.type(),
                    "data", request.data()
            );

            ResponseEntity<Void> response =
                    restClient.post()
                            .uri(request.destination())
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(payload)
                            .retrieve()
                            .toBodilessEntity();

            attempt.succeed(
                    response.getStatusCode().value()
            );

            attemptRepository.save(attempt);

            event.setStatus("delivered");
            eventRepository.save(event);

        } catch (RestClientResponseException e) {

            attempt.fail(
                    e.getStatusCode().value(),
                    e.getMessage()
            );

            attemptRepository.save(attempt);

            event.setStatus("failed");
            eventRepository.save(event);

        } catch (Exception e) {

            attempt.fail(
                    null,
                    e.getMessage()
            );

            attemptRepository.save(attempt);

            event.setStatus("failed");
            eventRepository.save(event);
        }
    }
}