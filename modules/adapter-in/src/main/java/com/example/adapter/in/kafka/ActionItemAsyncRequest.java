package com.example.adapter.in.kafka;

import com.example.avro.ActionItemAvro;
import lombok.*;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.support.Acknowledgment;

/**
 * Data transfer object representing an asynchronous action item request from Kafka.
 * <p>
 * Encapsulates the Kafka consumer record and acknowledgment for a single action item message.
 * Used throughout the message processing pipeline to carry both the message payload and
 * the acknowledgment handle for offset management.
 * <p>
 * <b>Responsibilities:</b>
 * <ul>
 *   <li>Holds the Kafka consumer record containing the Avro message</li>
 *   <li>Provides access to the acknowledgment for manual offset commits</li>
 *   <li>Overrides toString for concise logging and debugging</li>
 * </ul>
 * <b>Usage:</b> Passed between consumers, handlers, and processors to maintain context and
 * enable reliable message processing and acknowledgment.
 */
@Builder
@Getter
public class ActionItemAsyncRequest {

    private static final String REQUEST_FORMAT = "ActionItemAsyncRequest{topic='%s', partition=%d, offset=%d, key='%s'}";
    ConsumerRecord<String, ActionItemAvro> consumerRecord;
    Acknowledgment acknowledgment;

    @Override
    public String toString() {
        return REQUEST_FORMAT.formatted(consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset(), consumerRecord.key());
    }
}
