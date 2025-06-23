package com.example.application.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Database Health Monitor that controls Kafka consumers based on database availability.
 * 
 * This component:
 * 1. Monitors database health using the existing DatabaseConfig health check
 * 2. Pauses all Kafka consumers when database is down
 * 3. Resumes consumers when database recovers
 * 4. Provides configurable thresholds for downtime detection
 * 5. Implements circuit breaker pattern for consumer control
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseHealthMonitor {

    private final DatabaseConfig databaseConfig;
    private final ApplicationContext applicationContext;
    private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    // Configuration for downtime detection
    private static final long DOWNTIME_THRESHOLD_MS = 10000; // 10 seconds
    private static final long RECOVERY_THRESHOLD_MS = 5000;  // 5 seconds
    private static final int MAX_CONSECUTIVE_FAILURES = 3;

    // State management
    private final AtomicBoolean isDatabaseHealthy = new AtomicBoolean(true);
    private final AtomicBoolean consumersPaused = new AtomicBoolean(false);
    private final AtomicLong lastSuccessfulCheck = new AtomicLong(System.currentTimeMillis());
    private final AtomicLong lastFailureTime = new AtomicLong(0);
    private final AtomicLong consecutiveFailures = new AtomicLong(0);

    @PostConstruct
    public void initialize() {
        log.info("🔄 Database Health Monitor initialized");
        log.info("📊 Configuration - Downtime threshold: {}ms, Recovery threshold: {}ms, Max failures: {}", 
                DOWNTIME_THRESHOLD_MS, RECOVERY_THRESHOLD_MS, MAX_CONSECUTIVE_FAILURES);
        
        // Perform initial health check
        performHealthCheck();
    }

    /**
     * Scheduled health check that runs every 5 seconds
     */
    @Scheduled(fixedRate = 5000)
    public void scheduledHealthCheck() {
        performHealthCheck();
    }

    /**
     * Performs database health check and manages consumer state
     */
    public void performHealthCheck() {
        try {
            // Use the existing database health check mechanism
            databaseConfig.healthCheck();
            
            // If we reach here, database is healthy
            handleDatabaseHealthy();
            
        } catch (Exception e) {
            handleDatabaseUnhealthy(e);
        }
    }

    /**
     * Handles the case when database is healthy
     */
    private void handleDatabaseHealthy() {
        long currentTime = System.currentTimeMillis();
        lastSuccessfulCheck.set(currentTime);
        consecutiveFailures.set(0);
        
        boolean wasHealthy = isDatabaseHealthy.getAndSet(true);
        
        if (!wasHealthy) {
            log.info("✅ Database recovered! Last failure was {}ms ago", 
                    currentTime - lastFailureTime.get());
            
            // Check if we should resume consumers
            if (consumersPaused.get()) {
                long timeSinceRecovery = currentTime - lastFailureTime.get();
                if (timeSinceRecovery >= RECOVERY_THRESHOLD_MS) {
                    resumeConsumers();
                } else {
                    log.info("⏳ Waiting {}ms before resuming consumers (recovery threshold)", 
                            RECOVERY_THRESHOLD_MS - timeSinceRecovery);
                }
            }
        }
    }

    /**
     * Handles the case when database is unhealthy
     */
    private void handleDatabaseUnhealthy(Exception error) {
        long currentTime = System.currentTimeMillis();
        long failures = consecutiveFailures.incrementAndGet();
        lastFailureTime.set(currentTime);
        
        boolean wasHealthy = isDatabaseHealthy.getAndSet(false);
        
        if (wasHealthy) {
            log.warn("❌ Database health check failed: {}", error.getMessage());
        }
        
        // Check if we should pause consumers
        if (!consumersPaused.get() && failures >= MAX_CONSECUTIVE_FAILURES) {
            long timeSinceLastSuccess = currentTime - lastSuccessfulCheck.get();
            
            if (timeSinceLastSuccess >= DOWNTIME_THRESHOLD_MS) {
                pauseConsumers();
            } else {
                log.warn("⏳ Database failing but waiting {}ms before pausing consumers (downtime threshold)", 
                        DOWNTIME_THRESHOLD_MS - timeSinceLastSuccess);
            }
        }
    }

    /**
     * Pauses all Kafka consumers
     */
    private void pauseConsumers() {
        if (consumersPaused.compareAndSet(false, true)) {
            try {
                log.warn("🛑 PAUSING all Kafka consumers due to database downtime");
                
                Collection<MessageListenerContainer> containers = kafkaListenerEndpointRegistry.getAllListenerContainers();
                
                for (MessageListenerContainer container : containers) {
                    if (container.isRunning()) {
                        container.pause();
                        log.info("⏸️ Paused consumer: {} (topic: {})", 
                                container.getListenerId(), 
                                container.getAssignedPartitions());
                    }
                }
                
                log.warn("🛑 All {} Kafka consumers have been PAUSED", containers.size());
                
            } catch (Exception e) {
                log.error("❌ Failed to pause Kafka consumers", e);
                consumersPaused.set(false); // Reset state on error
            }
        }
    }

    /**
     * Resumes all Kafka consumers
     */
    private void resumeConsumers() {
        if (consumersPaused.compareAndSet(true, false)) {
            try {
                log.info("▶️ RESUMING all Kafka consumers after database recovery");
                
                Collection<MessageListenerContainer> containers = kafkaListenerEndpointRegistry.getAllListenerContainers();
                
                for (MessageListenerContainer container : containers) {
                    if (container.isPauseRequested()) {
                        container.resume();
                        log.info("▶️ Resumed consumer: {} (topic: {})", 
                                container.getListenerId(), 
                                container.getAssignedPartitions());
                    }
                }
                
                log.info("▶️ All {} Kafka consumers have been RESUMED", containers.size());
                
            } catch (Exception e) {
                log.error("❌ Failed to resume Kafka consumers", e);
                consumersPaused.set(true); // Reset state on error
            }
        }
    }

    /**
     * Gets current health status
     */
    public boolean isDatabaseHealthy() {
        return isDatabaseHealthy.get();
    }

    /**
     * Gets current consumer pause status
     */
    public boolean areConsumersPaused() {
        return consumersPaused.get();
    }

    /**
     * Gets time since last successful database check
     */
    public long getTimeSinceLastSuccess() {
        return System.currentTimeMillis() - lastSuccessfulCheck.get();
    }

    /**
     * Gets consecutive failure count
     */
    public long getConsecutiveFailures() {
        return consecutiveFailures.get();
    }

    /**
     * Logs current status for monitoring
     */
    public void logStatus() {
        log.info("📊 Database Health Monitor Status - " +
                "Healthy: {}, Consumers Paused: {}, " +
                "Time Since Success: {}ms, Consecutive Failures: {}", 
                isDatabaseHealthy.get(), 
                consumersPaused.get(),
                getTimeSinceLastSuccess(),
                consecutiveFailures.get());
    }
} 