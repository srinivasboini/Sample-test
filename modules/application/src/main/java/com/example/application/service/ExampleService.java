package com.example.application.service;

import com.example.commons.annotation.Run;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Example service demonstrating the usage of @Run annotation
 * This service shows how to apply the @Run annotation to methods
 */
@Service
@Slf4j
public class ExampleService {

    /**
     * Example method with @Run annotation using default message
     */
    @Run
    public void performDefaultAction() {
        log.info("Executing performDefaultAction method");
        // Some business logic here
        System.out.println("Default action performed successfully!");
    }

    /**
     * Example method with @Run annotation using custom message
     */
    @Run(message = "Custom run operation executed")
    public void performCustomAction() {
        log.info("Executing performCustomAction method");
        // Some business logic here
        System.out.println("Custom action performed successfully!");
    }

    /**
     * Example method with @Run annotation and parameters
     */
    @Run(message = "Processing user data")
    public String processUserData(String userId, String data) {
        log.info("Processing data for user: {}", userId);
        // Some business logic here
        String result = "Processed: " + data + " for user " + userId;
        System.out.println(result);
        return result;
    }

    /**
     * Regular method without @Run annotation for comparison
     */
    public void regularMethod() {
        log.info("Executing regular method without @Run annotation");
        System.out.println("Regular method executed - no @Run annotation triggered");
    }
} 