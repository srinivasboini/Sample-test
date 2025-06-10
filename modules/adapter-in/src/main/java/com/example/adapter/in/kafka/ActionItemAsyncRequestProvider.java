package com.example.adapter.in.kafka;

import com.example.avro.ActionItemAvro;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

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
