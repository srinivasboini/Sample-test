package com.example.application.controller;

import com.example.application.config.DatabaseHealthMonitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for monitoring database health and Kafka consumer status.
 * 
 * Provides endpoints to:
 * - Check current database health status
 * - Monitor Kafka consumer pause/resume state
 * - Get detailed health metrics
 * - Manually trigger health checks
 */
@Slf4j
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class DatabaseHealthController {

    private final DatabaseHealthMonitor databaseHealthMonitor;

    /**
     * Get current database health status
     */
    @GetMapping("/database")
    public ResponseEntity<Map<String, Object>> getDatabaseHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("healthy", databaseHealthMonitor.isDatabaseHealthy());
        response.put("timeSinceLastSuccess", databaseHealthMonitor.getTimeSinceLastSuccess());
        response.put("consecutiveFailures", databaseHealthMonitor.getConsecutiveFailures());
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get Kafka consumer status
     */
    @GetMapping("/consumers")
    public ResponseEntity<Map<String, Object>> getConsumerStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("paused", databaseHealthMonitor.areConsumersPaused());
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get comprehensive health status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getHealthStatus() {
        Map<String, Object> response = new HashMap<>();
        
        // Database health
        response.put("database", Map.of(
            "healthy", databaseHealthMonitor.isDatabaseHealthy(),
            "timeSinceLastSuccess", databaseHealthMonitor.getTimeSinceLastSuccess(),
            "consecutiveFailures", databaseHealthMonitor.getConsecutiveFailures()
        ));
        
        // Consumer status
        response.put("consumers", Map.of(
            "paused", databaseHealthMonitor.areConsumersPaused()
        ));
        
        // System info
        response.put("system", Map.of(
            "timestamp", System.currentTimeMillis(),
            "uptime", System.currentTimeMillis() - System.nanoTime() / 1_000_000
        ));
        
        return ResponseEntity.ok(response);
    }

    /**
     * Manually trigger a health check
     */
    @PostMapping("/check")
    public ResponseEntity<Map<String, Object>> triggerHealthCheck() {
        log.info("🔄 Manual health check triggered via REST API");
        
        try {
            databaseHealthMonitor.performHealthCheck();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Health check completed");
            response.put("databaseHealthy", databaseHealthMonitor.isDatabaseHealthy());
            response.put("consumersPaused", databaseHealthMonitor.areConsumersPaused());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Manual health check failed", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get detailed health metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getHealthMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Database metrics
        metrics.put("database.healthy", databaseHealthMonitor.isDatabaseHealthy());
        metrics.put("database.timeSinceLastSuccess", databaseHealthMonitor.getTimeSinceLastSuccess());
        metrics.put("database.consecutiveFailures", databaseHealthMonitor.getConsecutiveFailures());
        
        // Consumer metrics
        metrics.put("consumers.paused", databaseHealthMonitor.areConsumersPaused());
        
        // System metrics
        metrics.put("system.timestamp", System.currentTimeMillis());
        metrics.put("system.memory.used", Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
        metrics.put("system.memory.total", Runtime.getRuntime().totalMemory());
        metrics.put("system.memory.max", Runtime.getRuntime().maxMemory());
        
        return ResponseEntity.ok(metrics);
    }

    /**
     * Health check endpoint for load balancers
     */
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("OK");
    }
} 