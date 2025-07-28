package com.example.adapter.in.kafka.handler;

import com.example.adapter.in.kafka.ActionItemAsyncRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static java.util.Optional.ofNullable;

/**
 * Handles the result of asynchronous action item processing.
 * <p>
 * This handler is responsible for determining whether the processing of an action item was successful or resulted in an error,
 * and then delegating to the appropriate handler for success or error scenarios. It also ensures that Kafka offsets are committed
 * after processing, regardless of outcome, to maintain message delivery guarantees.
 *
 * <b>Responsibilities:</b>
 * <ul>
 *   <li>Delegates error handling to ActionItemProcessingErrorHandler if an error occurred.</li>
 *   <li>Logs successful processing events.</li>
 *   <li>Commits Kafka offsets via OffsetCommitter to acknowledge message consumption.</li>
 * </ul>
 *
 * <b>Usage:</b> Called after asynchronous message processing completes, typically from an async handler or callback.
 */
@Component
@RequiredArgsConstructor
@Slf4j
class ActionItemProcessingResultHandler {

    private final ActionItemProcessingErrorHandler processingErrorHandler;
    private final OffsetCommitter offsetCommitter;

    /**
     * Handles the result of processing an ActionItemAsyncRequest.
     * <p>
     * If an error is present, delegates to error handler; otherwise, logs success.
     * Always commits the Kafka offset for the processed message.
     *
     * @param request The async request that was processed
     * @param error The error that occurred during processing, or null if successful
     */
    public void handleResult(ActionItemAsyncRequest request, Throwable error) {
        ofNullable(error)
                .ifPresentOrElse(
                        throwable -> handleProcessingError(request, throwable),
                        () -> handleProcessingSuccess(request));

        offsetCommitter.commitOffset(request);

    }

    /**
     * Handles processing errors by logging and delegating to the error handler.
     *
     * @param request The async request that failed
     * @param error The error that occurred
     */
    private void handleProcessingError(ActionItemAsyncRequest request, Throwable error) {
        log.error("Processing failed for {}", request, error);
        processingErrorHandler.handleError(request, error);
    }

    /**
     * Handles successful processing by logging the event.
     *
     * @param request The async request that was processed successfully
     */
    private void handleProcessingSuccess(ActionItemAsyncRequest request) {
        log.info("Processing completed for {}", request);
        //offsetCommitter.commitOffset(request);
    }

}
