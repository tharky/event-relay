package com.tigerharkins.event_relay;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "events")
public class EventEntity {

    @Id
    private String id;

    private String type;

    private String destination;

    private String status;

    private int attempts;

    @Column(columnDefinition = "TEXT")
    private String payload;

    private Instant createdAt;

    private Instant updatedAt;

    protected EventEntity() {
    }

    public EventEntity(
            String id,
            String type,
            String destination,
            String payload) {

        this.id = id;
        this.type = type;
        this.destination = destination;
        this.payload = payload;
        this.status = "queued";
        this.attempts = 0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getDestination() {
        return destination;
    }

    public String getStatus() {
        return status;
    }

    public int getAttempts() {
        return attempts;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public void incrementAttempts() {
        this.attempts++;
        this.updatedAt = Instant.now();
    }
}