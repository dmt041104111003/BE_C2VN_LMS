package com.cardano_lms.server.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        long uptimeMillis = ManagementFactory.getRuntimeMXBean().getUptime();
        double uptimeSeconds = uptimeMillis / 1000.0;

        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "timestamp", Instant.now().toString(),
                "uptime", uptimeSeconds,
                "service", "lms-server"
        ));
    }
}

