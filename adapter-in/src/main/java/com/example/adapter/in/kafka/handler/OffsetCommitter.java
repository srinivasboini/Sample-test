package com.example.adapter.in.kafka.handler;

import com.example.adapter.in.kafka.ActionItemAsyncRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class OffsetCommitter {

    public void commitOffset(ActionItemAsyncRequest actionItemAsyncRequest) {
        try {
            Acknowledgment acknowledgment = actionItemAsyncRequest.getAcknowledgment();
            acknowledgment.acknowledge();
            log.info("Committed offset for {}", actionItemAsyncRequest);
        } catch (Exception e) {
            log.error("Failed to commit offset for {}", actionItemAsyncRequest, e);
        }
    }
}
