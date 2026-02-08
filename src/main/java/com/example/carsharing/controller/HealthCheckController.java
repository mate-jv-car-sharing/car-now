package com.example.carsharing.controller;

import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthCheckController {

    @GetMapping
    public Map<String, Object> getHealthStatus() {
        return Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now(),
                "message", "Car Sharing Service is running smoothly"
        );
    }
}
