package com.example.commons.mdc;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Utility class for MDC (Mapped Diagnostic Context) operations.
 * 
 * Provides convenient methods for managing MDC context throughout the application,
 * including correlation ID management, context preservation, and utility operations.
 * 
 * Features:
 * - Correlation ID generation and management
 * - Context preservation across method calls
 * - Utility methods for common MDC operations
 * - Thread-safe operations
 */
@Slf4j
public class MdcUtils {

    // Standard MDC keys
    public static final String CORRELATION_ID_KEY = "correlationId";
    public static final String REQUEST_ID_KEY = "requestId";
    public static final String USER_ID_KEY = "userId";
    public static final String SESSION_ID_KEY = "sessionId";
    public static final String THREAD_ID_KEY = "threadId";
    public static final String COMPONENT_KEY = "component";
    public static final String OPERATION_KEY = "operation";
    
    // Kafka-specific MDC keys
    public static final String KAFKA_TOPIC_KEY = "kafka.topic";
    public static final String KAFKA_PARTITION_KEY = "kafka.partition";
    public static final String KAFKA_OFFSET_KEY = "kafka.offset";
    public static final String KAFKA_MESSAGE_KEY = "kafka.messageKey";
    public static final String KAFKA_TIMESTAMP_KEY = "kafka.timestamp";
    public static final String KAFKA_CONSUMER_GROUP_KEY = "kafka.consumerGroup";

    /**
     * Generates and sets a new correlation ID.
     * 
     * @return The generated correlation ID
     */
    public static String setCorrelationId() {
        String correlationId = UUID.randomUUID().toString();
        MDC.put(CORRELATION_ID_KEY, correlationId);
        log.debug("Set correlation ID: {}", correlationId);
        return correlationId;
    }

    /**
     * Sets a specific correlation ID.
     * 
     * @param correlationId The correlation ID to set
     */
    public static void setCorrelationId(String correlationId) {
        if (correlationId != null && !correlationId.trim().isEmpty()) {
            MDC.put(CORRELATION_ID_KEY, correlationId);
            log.debug("Set correlation ID: {}", correlationId);
        }
    }

    /**
     * Gets the current correlation ID.
     * 
     * @return The current correlation ID or null if not set
     */
    public static String getCorrelationId() {
        return MDC.get(CORRELATION_ID_KEY);
    }

    /**
     * Sets a request ID.
     * 
     * @param requestId The request ID to set
     */
    public static void setRequestId(String requestId) {
        if (requestId != null && !requestId.trim().isEmpty()) {
            MDC.put(REQUEST_ID_KEY, requestId);
        }
    }

    /**
     * Gets the current request ID.
     * 
     * @return The current request ID or null if not set
     */
    public static String getRequestId() {
        return MDC.get(REQUEST_ID_KEY);
    }

    /**
     * Sets a user ID.
     * 
     * @param userId The user ID to set
     */
    public static void setUserId(String userId) {
        if (userId != null && !userId.trim().isEmpty()) {
            MDC.put(USER_ID_KEY, userId);
        }
    }

    /**
     * Gets the current user ID.
     * 
     * @return The current user ID or null if not set
     */
    public static String getUserId() {
        return MDC.get(USER_ID_KEY);
    }

    /**
     * Sets a session ID.
     * 
     * @param sessionId The session ID to set
     */
    public static void setSessionId(String sessionId) {
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            MDC.put(SESSION_ID_KEY, sessionId);
        }
    }

    /**
     * Gets the current session ID.
     * 
     * @return The current session ID or null if not set
     */
    public static String getSessionId() {
        return MDC.get(SESSION_ID_KEY);
    }

    /**
     * Sets the current thread ID.
     */
    public static void setThreadId() {
        MDC.put(THREAD_ID_KEY, Thread.currentThread().getName());
    }

    /**
     * Gets the current thread ID.
     * 
     * @return The current thread ID or null if not set
     */
    public static String getThreadId() {
        return MDC.get(THREAD_ID_KEY);
    }

    /**
     * Sets a component identifier.
     * 
     * @param component The component identifier
     */
    public static void setComponent(String component) {
        if (component != null && !component.trim().isEmpty()) {
            MDC.put(COMPONENT_KEY, component);
        }
    }

    /**
     * Gets the current component identifier.
     * 
     * @return The current component identifier or null if not set
     */
    public static String getComponent() {
        return MDC.get(COMPONENT_KEY);
    }

    /**
     * Sets an operation identifier.
     * 
     * @param operation The operation identifier
     */
    public static void setOperation(String operation) {
        if (operation != null && !operation.trim().isEmpty()) {
            MDC.put(OPERATION_KEY, operation);
        }
    }

    /**
     * Gets the current operation identifier.
     * 
     * @return The current operation identifier or null if not set
     */
    public static String getOperation() {
        return MDC.get(OPERATION_KEY);
    }

    /**
     * Sets Kafka-specific context.
     * 
     * @param topic The Kafka topic
     * @param partition The partition number
     * @param offset The offset
     * @param messageKey The message key
     * @param timestamp The timestamp
     * @param consumerGroup The consumer group
     */
    public static void setKafkaContext(String topic, int partition, long offset, 
                                     String messageKey, long timestamp, String consumerGroup) {
        if (topic != null) MDC.put(KAFKA_TOPIC_KEY, topic);
        MDC.put(KAFKA_PARTITION_KEY, String.valueOf(partition));
        MDC.put(KAFKA_OFFSET_KEY, String.valueOf(offset));
        MDC.put(KAFKA_MESSAGE_KEY, messageKey != null ? messageKey : "null");
        MDC.put(KAFKA_TIMESTAMP_KEY, String.valueOf(timestamp));
        if (consumerGroup != null) MDC.put(KAFKA_CONSUMER_GROUP_KEY, consumerGroup);
    }

    /**
     * Clears Kafka-specific context.
     */
    public static void clearKafkaContext() {
        MDC.remove(KAFKA_TOPIC_KEY);
        MDC.remove(KAFKA_PARTITION_KEY);
        MDC.remove(KAFKA_OFFSET_KEY);
        MDC.remove(KAFKA_MESSAGE_KEY);
        MDC.remove(KAFKA_TIMESTAMP_KEY);
        MDC.remove(KAFKA_CONSUMER_GROUP_KEY);
    }

    /**
     * Gets a copy of the current MDC context.
     * 
     * @return A copy of the current MDC context map
     */
    public static Map<String, String> getContext() {
        return MDC.getCopyOfContextMap();
    }

    /**
     * Sets the MDC context from a map.
     * 
     * @param contextMap The context map to set
     */
    public static void setContext(Map<String, String> contextMap) {
        if (contextMap != null) {
            MDC.setContextMap(contextMap);
        }
    }

    /**
     * Clears all MDC context.
     */
    public static void clear() {
        MDC.clear();
    }

    /**
     * Executes a supplier with preserved MDC context.
     * 
     * @param supplier The supplier to execute
     * @param <T> The return type
     * @return The result of the supplier
     */
    public static <T> T withContext(Supplier<T> supplier) {
        Map<String, String> context = getContext();
        try {
            return supplier.get();
        } finally {
            if (context != null) {
                setContext(context);
            }
        }
    }

    /**
     * Executes a runnable with preserved MDC context.
     * 
     * @param runnable The runnable to execute
     */
    public static void withContext(Runnable runnable) {
        Map<String, String> context = getContext();
        try {
            runnable.run();
        } finally {
            if (context != null) {
                setContext(context);
            }
        }
    }

    /**
     * Creates a new MDC context with the given key-value pairs.
     * 
     * @param keyValues Key-value pairs to set in MDC
     */
    public static void putAll(String... keyValues) {
        if (keyValues != null && keyValues.length % 2 == 0) {
            for (int i = 0; i < keyValues.length; i += 2) {
                if (keyValues[i] != null && keyValues[i + 1] != null) {
                    MDC.put(keyValues[i], keyValues[i + 1]);
                }
            }
        }
    }

    /**
     * Removes multiple keys from MDC.
     * 
     * @param keys The keys to remove
     */
    public static void removeAll(String... keys) {
        if (keys != null) {
            for (String key : keys) {
                if (key != null) {
                    MDC.remove(key);
                }
            }
        }
    }

    /**
     * Logs the current MDC context for debugging purposes.
     */
    public static void logCurrentContext() {
        Map<String, String> context = getContext();
        if (context != null && !context.isEmpty()) {
            log.debug("Current MDC context: {}", context);
        } else {
            log.debug("No MDC context set");
        }
    }
} 