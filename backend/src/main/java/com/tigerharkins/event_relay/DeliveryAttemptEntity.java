package com.tigerharkins.event_relay;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "delivery_attempts")
public class DeliveryAttemptEntity {

    @Id
    private String id;

    private String eventId;

    private int attemptNumber;

    private String status;

    private Integer responseCode;

    private String errorMessage;

    private Instant startedAt;

    private Instant completedAt;

    protected DeliveryAttemptEntity() {
    }

    public DeliveryAttemptEntity(
            String eventId,
            int attemptNumber) {

        this.id = UUID.randomUUID().toString();
        this.eventId = eventId;
        this.attemptNumber = attemptNumber;
        this.status = "delivering";
        this.startedAt = Instant.now();
    }

    public void succeed(int responseCode) {
        this.status = "delivered";
        this.responseCode = responseCode;
        this.completedAt = Instant.now();
    }

    public void fail(Integer responseCode, String errorMessage) {
        this.status = "failed";
        this.responseCode = responseCode;
        this.errorMessage = errorMessage;
        this.completedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }

    public String getStatus() {
        return status;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}