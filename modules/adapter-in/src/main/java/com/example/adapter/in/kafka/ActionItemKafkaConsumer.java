package com.example.adapter.in.kafka;

import com.example.adapter.in.kafka.handler.ActionItemAsyncMessageHandler;
import com.example.adapter.in.kafka.handler.MessageHandler;
import com.example.avro.ActionItemAvro;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

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
 * </ul>
 * <p>
 * Processing Flow:
 * <ol>
 *   <li>Receives Avro-formatted messages from Kafka</li>
 *   <li>Processes messages asynchronously</li>
 *   <li>Handles success/failure scenarios</li>
 *   <li>Manages offset commitments</li>
 *   <li>Provides detailed logging</li>
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Observed
public class ActionItemKafkaConsumer {

    private final MessageHandler<ActionItemAsyncRequest> messageHandler;
    private final ActionItemAsyncRequestProvider actionItemAsyncRequestProvider;


    public void consume(ConsumerRecord<String, ActionItemAvro> record, Acknowledgment acknowledgment) {
        log.info("Received message with key: {} from topic: {} partition: {} offset: {}", record.key(),
                record.topic(), record.partition(), record.offset());
        
                MDC.getCopyOfContextMap();
        ActionItemAsyncRequest actionItemAsyncRequest = actionItemAsyncRequestProvider.getActionItemAsyncRequest(record, acknowledgment);
        messageHandler.handle(actionItemAsyncRequest);
    }
}
