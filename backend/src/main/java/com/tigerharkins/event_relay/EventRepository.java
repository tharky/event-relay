package com.tigerharkins.event_relay;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository
        extends JpaRepository<EventEntity, String> {
}