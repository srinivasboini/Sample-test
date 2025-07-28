package com.example.adapter.in.kafka.handler;

import com.example.adapter.in.kafka.ActionItemAsyncRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Commits Kafka offsets for processed action item requests.
 * <p>
 * This component is responsible for acknowledging message consumption to Kafka by committing offsets
 * after successful or failed processing. This ensures that messages are not reprocessed and that
 * at-least-once delivery semantics are maintained.
 *
 * <b>Responsibilities:</b>
 * <ul>
 *   <li>Commits offsets for processed messages using the Acknowledgment interface.</li>
 *   <li>Logs successful and failed offset commits for observability.</li>
 *   <li>Handles exceptions during offset commit to avoid message loss or duplication.</li>
 * </ul>
 *
 * <b>Usage:</b> Called by result handlers or async message handlers after processing completes.
 */
@Slf4j
@Component
class OffsetCommitter {
    /**
     * Commits the Kafka offset for the given async request.
     * <p>
     * Uses the Acknowledgment interface to acknowledge message consumption. Logs the outcome.
     *
     * @param actionItemAsyncRequest The async request whose offset should be committed
     */
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
