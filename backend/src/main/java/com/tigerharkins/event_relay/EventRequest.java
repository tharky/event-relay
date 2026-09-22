package com.tigerharkins.event_relay;

import java.util.Map;

public record EventRequest(
        String destination,
        String type,
        Map<String, Object> data
) {}