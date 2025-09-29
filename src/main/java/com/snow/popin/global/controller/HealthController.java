package com.snow.popin.global.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health(){
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "POPIN Backend is running");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info(){
        Map<String, Object> response = new HashMap<>();
        response.put("application", "POPIN");
        response.put("version", "1.0.0");
        response.put("environment", "production");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
}
