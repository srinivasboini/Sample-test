package com.example.adapter.in.kafka;

import com.example.avro.ActionItemAvro;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Provides ActionItemAsyncRequest objects for Kafka message processing.
 * <p>
 * This component is responsible for constructing ActionItemAsyncRequest instances
 * from Kafka consumer records and acknowledgments. It acts as a factory to encapsulate
 * the creation logic and ensure consistency across the application.
 * <p>
 * <b>Responsibilities:</b>
 * <ul>
 *   <li>Creates ActionItemAsyncRequest objects from Kafka records and acknowledgments</li>
 *   <li>Encapsulates construction logic for async requests</li>
 *   <li>Ensures all required fields are set for downstream processing</li>
 * </ul>
 * <b>Usage:</b> Used by Kafka consumers and message handlers to obtain properly constructed
 * async request objects for further processing.
 */
@Component
public class ActionItemAsyncRequestProvider {

    public ActionItemAsyncRequest getActionItemAsyncRequest(ConsumerRecord<String, ActionItemAvro> record, Acknowledgment acknowledgment) {
        return ActionItemAsyncRequest
                .builder()
                .consumerRecord(record)
                .acknowledgment(acknowledgment)
                .build();
    }
}
