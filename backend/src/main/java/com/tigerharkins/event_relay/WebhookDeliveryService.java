package com.tigerharkins.event_relay;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class WebhookDeliveryService {

    private static final Logger log =
            LoggerFactory.getLogger(WebhookDeliveryService.class);

    private static final int MAX_ATTEMPTS = 3;
    private static final long INITIAL_BACKOFF_MS = 1_000;

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

        Map<String, Object> payload = Map.of(
                "eventId", eventId,
                "type", request.type(),
                "data", request.data()
        );

        while (event.getAttempts() < MAX_ATTEMPTS) {

            event.setStatus("delivering");
            event.incrementAttempts();
            eventRepository.save(event);

            int attemptNumber = event.getAttempts();

            DeliveryAttemptEntity attempt =
                    new DeliveryAttemptEntity(
                            eventId,
                            attemptNumber
                    );

            attemptRepository.save(attempt);

            log.info(
                    "Delivering event {} - attempt {}/{}",
                    eventId,
                    attemptNumber,
                    MAX_ATTEMPTS
            );

            try {

                ResponseEntity<Void> response =
                        restClient.post()
                                .uri(request.destination())
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(payload)
                                .retrieve()
                                .toBodilessEntity();

                int responseCode =
                        response.getStatusCode().value();

                if (!response.getStatusCode().is2xxSuccessful()) {

                    attempt.fail(
                            responseCode,
                            "Unexpected HTTP status " + responseCode
                    );

                    attemptRepository.save(attempt);

                    boolean shouldContinue =
                            prepareRetryOrFail(
                                    event,
                                    attemptNumber,
                                    isRetryableStatus(responseCode),
                                    "HTTP " + responseCode
                            );

                    if (!shouldContinue) {
                        return;
                    }

                    continue;
                }

                attempt.succeed(responseCode);
                attemptRepository.save(attempt);

                event.setStatus("delivered");
                eventRepository.save(event);

                log.info(
                        "Event {} delivered successfully on attempt {}",
                        eventId,
                        attemptNumber
                );

                return;

            } catch (RestClientResponseException e) {

                int responseCode =
                        e.getStatusCode().value();

                attempt.fail(
                        responseCode,
                        e.getMessage()
                );

                attemptRepository.save(attempt);

                boolean shouldContinue =
                        prepareRetryOrFail(
                                event,
                                attemptNumber,
                                isRetryableStatus(responseCode),
                                "HTTP " + responseCode
                        );

                if (!shouldContinue) {
                    return;
                }

            } catch (ResourceAccessException e) {

                /*
                 * Connection refused, connection timeout,
                 * read timeout, DNS/network problems, etc.
                 *
                 * These may be temporary, so we retry them.
                 */
                attempt.fail(
                        null,
                        e.getMessage()
                );

                attemptRepository.save(attempt);

                boolean shouldContinue =
                        prepareRetryOrFail(
                                event,
                                attemptNumber,
                                true,
                                "network error: " + e.getMessage()
                        );

                if (!shouldContinue) {
                    return;
                }

            } catch (Exception e) {

                /*
                 * Unknown programming/configuration problems
                 * should not automatically be retried.
                 */
                attempt.fail(
                        null,
                        e.getMessage()
                );

                attemptRepository.save(attempt);

                event.setStatus("failed");
                eventRepository.save(event);

                log.error(
                        "Event {} failed with a non-retryable error",
                        eventId,
                        e
                );

                return;
            }
        }
    }

    private boolean prepareRetryOrFail(
            EventEntity event,
            int attemptNumber,
            boolean retryable,
            String failureDescription) {

        if (!retryable) {

            event.setStatus("failed");
            eventRepository.save(event);

            log.warn(
                    "Event {} permanently failed on attempt {}: {}",
                    event.getId(),
                    attemptNumber,
                    failureDescription
            );

            return false;
        }

        if (attemptNumber >= MAX_ATTEMPTS) {

            event.setStatus("failed");
            eventRepository.save(event);

            log.warn(
                    "Event {} exhausted all {} attempts. Last failure: {}",
                    event.getId(),
                    MAX_ATTEMPTS,
                    failureDescription
            );

            return false;
        }

        long backoffMs =
                calculateBackoffMs(attemptNumber);

        event.setStatus("retrying");
        eventRepository.save(event);

        log.warn(
                "Event {} attempt {}/{} failed: {}. Retrying in {} ms",
                event.getId(),
                attemptNumber,
                MAX_ATTEMPTS,
                failureDescription,
                backoffMs
        );

        try {

            Thread.sleep(backoffMs);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            event.setStatus("failed");
            eventRepository.save(event);

            log.warn(
                    "Retry process for event {} was interrupted",
                    event.getId()
            );

            return false;
        }

        return true;
    }

    private boolean isRetryableStatus(int statusCode) {

        return statusCode == 408
                || statusCode == 425
                || statusCode == 429
                || statusCode >= 500;
    }

    private long calculateBackoffMs(int attemptNumber) {

        return INITIAL_BACKOFF_MS
                * (1L << (attemptNumber - 1));
    }
}