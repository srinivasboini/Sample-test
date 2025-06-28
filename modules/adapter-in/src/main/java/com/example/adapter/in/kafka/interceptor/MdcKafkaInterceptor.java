package com.example.adapter.in.kafka.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Kafka Consumer Interceptor for MDC (Mapped Diagnostic Context) setup.
 * 
 * This interceptor automatically sets up MDC context for each Kafka message,
 * providing correlation IDs, topic information, and other metadata for
 * distributed tracing and logging.
 * 
 * Features:
 * - Automatic correlation ID generation
 * - Topic, partition, and offset tracking
 * - Consumer group information
 * - Message key and timestamp tracking
 * - Thread-safe MDC context management
 */
@Slf4j
@Component
public class MdcKafkaInterceptor implements ConsumerInterceptor<String, Object> {

    private static final String CORRELATION_ID_KEY = "correlationId";
    private static final String TOPIC_KEY = "kafka.topic";
    private static final String PARTITION_KEY = "kafka.partition";
    private static final String OFFSET_KEY = "kafka.offset";
    private static final String MESSAGE_KEY = "kafka.messageKey";
    private static final String TIMESTAMP_KEY = "kafka.timestamp";
    private static final String CONSUMER_GROUP_KEY = "kafka.consumerGroup";
    private static final String THREAD_ID_KEY = "threadId";

    @Override
    public ConsumerRecords<String, Object> onConsume(ConsumerRecords<String, Object> records) {
        if (records.isEmpty()) {
            return records;
        }

        // Set up MDC context for the current thread
        setupMdcContext(records);
        
        log.debug("MDC context set up for {} records", records.count());
        
        return records;
    }

    @Override
    public void onCommit(Map<TopicPartition, OffsetAndMetadata> offsets) {
        // Log commit information with MDC context
        log.debug("Committing offsets: {}", offsets);
    }

    @Override
    public void close() {
        // Clean up MDC context when interceptor is closed
        MDC.clear();
        log.debug("MDC context cleared on interceptor close");
    }

    @Override
    public void configure(Map<String, ?> configs) {
        // Configuration can be added here if needed
        log.debug("MdcKafkaInterceptor configured with {} configs", configs.size());
    }

    /**
     * Sets up MDC context for the given consumer records.
     * 
     * @param records The consumer records to set up context for
     */
    private void setupMdcContext(ConsumerRecords<String, Object> records) {
        try {
            // Generate a correlation ID for this batch of records
            String correlationId = generateCorrelationId();
            MDC.put(CORRELATION_ID_KEY, correlationId);
            
            // Set thread ID for tracking
            MDC.put(THREAD_ID_KEY, Thread.currentThread().getName());
            
            // Set consumer group if available (this would need to be passed in or configured)
            // For now, we'll use a default value
            MDC.put(CONSUMER_GROUP_KEY, "action-item-consumer-group");
            
            // For the first record, set common context
            ConsumerRecord<String, Object> firstRecord = records.iterator().next();
            setRecordContext(firstRecord);
            
            log.debug("MDC context initialized - correlationId: {}, topic: {}, partition: {}", 
                     correlationId, firstRecord.topic(), firstRecord.partition());
            
        } catch (Exception e) {
            log.error("Failed to set up MDC context", e);
            // Don't throw the exception to avoid breaking the consumer
        }
    }

    /**
     * Sets MDC context for a specific consumer record.
     * 
     * @param record The consumer record to set context for
     */
    public static void setRecordContext(ConsumerRecord<String, Object> record) {
        try {
            MDC.put(TOPIC_KEY, record.topic());
            MDC.put(PARTITION_KEY, String.valueOf(record.partition()));
            MDC.put(OFFSET_KEY, String.valueOf(record.offset()));
            MDC.put(MESSAGE_KEY, record.key() != null ? record.key() : "null");
            MDC.put(TIMESTAMP_KEY, String.valueOf(record.timestamp()));
            
        } catch (Exception e) {
            log.error("Failed to set record context for topic: {}, partition: {}, offset: {}", 
                     record.topic(), record.partition(), record.offset(), e);
        }
    }

    /**
     * Updates MDC context for a specific record during processing.
     * This method should be called when processing individual records.
     * 
     * @param record The consumer record being processed
     */
    public static void updateRecordContext(ConsumerRecord<String, Object> record) {
        setRecordContext(record);
    }

    /**
     * Clears record-specific MDC context while preserving correlation ID.
     */
    public static void clearRecordContext() {
        MDC.remove(TOPIC_KEY);
        MDC.remove(PARTITION_KEY);
        MDC.remove(OFFSET_KEY);
        MDC.remove(MESSAGE_KEY);
        MDC.remove(TIMESTAMP_KEY);
    }

    /**
     * Clears all MDC context including correlation ID.
     */
    public static void clearAllContext() {
        MDC.clear();
    }

    /**
     * Gets the current correlation ID from MDC.
     * 
     * @return The correlation ID or null if not set
     */
    public static String getCorrelationId() {
        return MDC.get(CORRELATION_ID_KEY);
    }

    /**
     * Sets a specific correlation ID in MDC.
     * 
     * @param correlationId The correlation ID to set
     */
    public static void setCorrelationId(String correlationId) {
        MDC.put(CORRELATION_ID_KEY, correlationId);
    }

    /**
     * Generates a unique correlation ID.
     * 
     * @return A unique correlation ID
     */
    private String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Creates a copy of the current MDC context.
     * 
     * @return A copy of the current MDC context map
     */
    public static Map<String, String> getMdcContext() {
        return MDC.getCopyOfContextMap();
    }

    /**
     * Restores MDC context from a previously saved context map.
     * 
     * @param contextMap The context map to restore
     */
    public static void restoreMdcContext(Map<String, String> contextMap) {
        if (contextMap != null) {
            MDC.setContextMap(contextMap);
        }
    }
} 