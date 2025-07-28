package com.example.commons.async;

import io.micrometer.context.ContextExecutorService;
import io.micrometer.context.ContextSnapshot;
import io.micrometer.context.ContextSnapshotFactory;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Configuration for asynchronous operations with context propagation.
 * 
 * This configuration ensures that MDC context and other contextual information
 * are properly propagated across async operations for distributed tracing.
 */
@Configuration
public class AsyncConfig {

    /**
     * Task decorator that preserves MDC context across async operations.
     */
    @Bean
    public TaskDecorator mdcTaskDecorator() {
        return runnable -> {
            Map<String, String> contextMap = MDC.getCopyOfContextMap();
            return () -> {
                try {
                    if (contextMap != null) {
                        MDC.setContextMap(contextMap);
                    }
                    runnable.run();
                } finally {
                    MDC.clear();
                }
            };
        };
    }

    /**
     * Primary executor for message processing with MDC context preservation.
     * 
     * @return Executor with context propagation
     */
    @Bean("messageProcessingExecutor")
    @Primary
    public Executor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("kafka-msg-processor-");
        
        // Set the MDC task decorator for context preservation
        executor.setTaskDecorator(mdcTaskDecorator());
        
        executor.initialize();
        
        // Wrap with Micrometer context propagation for additional context types
        return ContextExecutorService.wrap(
            executor.getThreadPoolExecutor(), 
            ContextSnapshotFactory.builder().build()::captureAll
        );
    }

    /**
     * Secondary executor for general async operations with MDC context preservation.
     * 
     * @return Executor for general async operations
     */
    @Bean("generalAsyncExecutor")
    public Executor generalAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("general-async-");
        
        // Set the MDC task decorator for context preservation
        executor.setTaskDecorator(mdcTaskDecorator());
        
        executor.initialize();
        
        return ContextExecutorService.wrap(
            executor.getThreadPoolExecutor(), 
            ContextSnapshotFactory.builder().build()::captureAll
        );
    }
}
