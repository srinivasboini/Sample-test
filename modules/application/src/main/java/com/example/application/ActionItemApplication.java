package com.example.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Action Item Application.
 * <p>
 * This class bootstraps the Spring Boot application, enabling component scanning and scheduling.
 * <ul>
 *   <li>@SpringBootApplication: Marks this as a Spring Boot application, enabling auto-configuration and component scanning.</li>
 *   <li>@ComponentScan: Scans the base package 'com.example' for Spring components.</li>
 *   <li>@EnableScheduling: Enables scheduled tasks within the application context.</li>
 * </ul>
 * <p>
 * The main method delegates to SpringApplication.run(), which starts the embedded server and initializes the application context.
 *
 * Usage:
 * <pre>
 *     java -jar application-1.0.0-SNAPSHOT.jar
 * </pre>
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.example")
@EnableScheduling
public class ActionItemApplication {
    /**
     * Main method to launch the Spring Boot application.
     *
     * @param args Command-line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(ActionItemApplication.class, args);
    }
}
