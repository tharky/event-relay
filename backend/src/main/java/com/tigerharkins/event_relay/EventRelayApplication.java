package com.tigerharkins.event_relay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EventRelayApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventRelayApplication.class, args);
    }
}