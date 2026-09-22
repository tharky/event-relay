package com.tigerharkins.event_relay;

public class EventStatus {

    private final String eventId;
    private String status;
    private int attempts;

    public EventStatus(String eventId) {
        this.eventId = eventId;
        this.status = "queued";
        this.attempts = 0;
    }

    public String getEventId() {
        return eventId;
    }

    public String getStatus() {
        return status;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void incrementAttempts() {
        this.attempts++;
    }
}