package com.tigerharkins.event_relay;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class EventStore {

    private final Map<String, EventStatus> events = new ConcurrentHashMap<>();

    public void save(EventStatus event) {
        events.put(event.getEventId(), event);
    }

    public EventStatus get(String eventId) {
        return events.get(eventId);
    }
}