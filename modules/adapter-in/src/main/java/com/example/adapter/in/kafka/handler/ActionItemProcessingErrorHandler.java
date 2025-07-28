package com.example.adapter.in.kafka.handler;

import com.example.adapter.in.kafka.ActionItemAsyncRequest;
import com.example.port.in.HandleProcessingErrorUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handles errors that occur during asynchronous action item processing.
 * <p>
 * This handler is responsible for logging errors and delegating error persistence to the application layer
 * via the HandleProcessingErrorUseCase port. It ensures that all processing errors are captured and stored
 * for later analysis or alerting.
 *
 * <b>Responsibilities:</b>
 * <ul>
 *   <li>Logs errors that occur during async processing.</li>
 *   <li>Delegates error persistence to the application service via HandleProcessingErrorUseCase.</li>
 *   <li>Handles and logs any failures that occur during error persistence.</li>
 * </ul>
 *
 * <b>Usage:</b> Called by result handlers or async message handlers when an error is encountered during processing.
 */
@Slf4j
@Component
@RequiredArgsConstructor
class ActionItemProcessingErrorHandler {

    private final HandleProcessingErrorUseCase handleProcessingErrorUseCase;

    /**
     * Handles an error that occurred during async processing by logging and persisting it.
     *
     * @param actionItemAsyncRequest The async request that failed
     * @param error The error that occurred
     */
    public void handleError(ActionItemAsyncRequest actionItemAsyncRequest, Throwable error) {
        log.error("Error processing {}", actionItemAsyncRequest, error);
        
        try {
            handleProcessingErrorUseCase.handleError(
                "KAFKA_CONSUMER",
                error,
                actionItemAsyncRequest.toString()
            );
            log.info("Successfully handled error for request: {}", actionItemAsyncRequest);
        } catch (Exception e) {
            log.error("Failed to handle error for request: {}", actionItemAsyncRequest, e);
        }
    }
}
