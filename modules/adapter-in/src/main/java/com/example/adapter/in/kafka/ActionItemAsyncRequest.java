package com.example.adapter.in.kafka;

import com.example.avro.ActionItemAvro;
import lombok.*;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.support.Acknowledgment;

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
