package com.example.adapter.in.kafka;

import com.example.adapter.in.kafka.handler.ActionItemAsyncMessageHandler;
import com.example.adapter.in.kafka.handler.MessageHandler;
import com.example.adapter.in.kafka.interceptor.MdcKafkaInterceptor;
import com.example.avro.ActionItemAvro;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Kafka Consumer for Action Items with asynchronous processing capabilities.
 * <p>
 * Features:
 * <ul>
 *   <li>Asynchronous processing using CompletableFuture</li>
 *   <li>Manual offset management for reliability</li>
 *   <li>Comprehensive error handling and logging</li>
 *   <li>Thread pool execution for scalability</li>
 *   <li>Observation and monitoring support</li>
 *   <li>MDC (Mapped Diagnostic Context) integration for distributed tracing</li>
 * </ul>
 * <p>
 * Processing Flow:
 * <ol>
 *   <li>Receives Avro-formatted messages from Kafka</li>
 *   <li>Sets up MDC context for correlation and tracing</li>
 *   <li>Processes messages asynchronously</li>
 *   <li>Handles success/failure scenarios</li>
 *   <li>Manages offset commitments</li>
 *   <li>Provides detailed logging with context</li>
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Observed
public class ActionItemKafkaConsumer {

    private final MessageHandler<ActionItemAsyncRequest> messageHandler;
    private final ActionItemAsyncRequestProvider actionItemAsyncRequestProvider;

    /**
     * Consumes Kafka messages with MDC context setup for distributed tracing.
     * 
     * @param record The Kafka consumer record
     * @param acknowledgment The acknowledgment for manual offset management
     */
    public void consume(ConsumerRecord<String, ActionItemAvro> record, Acknowledgment acknowledgment) {
        // Capture the current MDC context (set by the interceptor)
        Map<String, String> mdcContext = MdcKafkaInterceptor.getMdcContext();
        
        try {
            // Update MDC context with specific record information
            
            log.info("Received message with key: {} from topic: {} partition: {} offset: {} correlationId: {}", 
                    record.key(), record.topic(), record.partition(), record.offset(), 
                    MdcKafkaInterceptor.getCorrelationId());
            
            // Create the async request
            ActionItemAsyncRequest actionItemAsyncRequest = actionItemAsyncRequestProvider.getActionItemAsyncRequest(record, acknowledgment);
            
            // Handle the message with preserved MDC context
            messageHandler.handle(actionItemAsyncRequest);
            
            log.info("Successfully processed message for topic: {} partition: {} offset: {}", 
                    record.topic(), record.partition(), record.offset());
                    
        } catch (Exception e) {
            log.error("Failed to process message for topic: {} partition: {} offset: {} correlationId: {}", 
                     record.topic(), record.partition(), record.offset(), 
                     MdcKafkaInterceptor.getCorrelationId(), e);
            throw e;
        } 
    }
}
