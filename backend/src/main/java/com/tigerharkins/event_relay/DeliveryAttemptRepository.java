package com.tigerharkins.event_relay;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryAttemptRepository
        extends JpaRepository<DeliveryAttemptEntity, String> {

    List<DeliveryAttemptEntity> findByEventIdOrderByAttemptNumberAsc(
            String eventId);
}