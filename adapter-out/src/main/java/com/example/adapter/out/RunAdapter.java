package com.example.adapter.out;

import com.example.port.out.RunPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Adapter implementation for RunPort
 * This adapter provides the concrete implementation for the @Run annotation operations
 * Currently just prints information - no business logic implemented yet
 */
@Slf4j
@Component
public class RunAdapter implements RunPort {

    @Override
    public void executeRun(String message, String methodName, String className) {
        // Simple implementation that just prints information
        // In the future, this can be extended with actual business logic
        log.info("🚀 Run annotation triggered!");
        log.info("📍 Class: {}", className);
        log.info("🔧 Method: {}", methodName);
        log.info("💬 Message: {}", message);
        log.info("⏰ Timestamp: {}", java.time.Instant.now());
        
        // Placeholder for future business logic
        System.out.println("=".repeat(50));
        System.out.println("RUN ADAPTER EXECUTED");
        System.out.printf("Class: %s%n", className);
        System.out.printf("Method: %s%n", methodName);
        System.out.printf("Message: %s%n", message);
        System.out.println("=".repeat(50));
    }
} 