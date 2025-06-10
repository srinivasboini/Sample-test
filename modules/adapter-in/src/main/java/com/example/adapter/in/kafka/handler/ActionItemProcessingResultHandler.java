package com.example.adapter.in.kafka.handler;

import com.example.adapter.in.kafka.ActionItemAsyncRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static java.util.Optional.ofNullable;

@Component
@RequiredArgsConstructor
@Slf4j
class ActionItemProcessingResultHandler {

    private final ActionItemProcessingErrorHandler processingErrorHandler;
    private final OffsetCommitter offsetCommitter;

    public void handleResult(ActionItemAsyncRequest request, Throwable error) {
        ofNullable(error)
                .ifPresentOrElse(
                        throwable -> handleProcessingError(request, throwable),
                        () -> handleProcessingSuccess(request));

        offsetCommitter.commitOffset(request);

    }

    private void handleProcessingError(ActionItemAsyncRequest request, Throwable error) {
        log.error("Processing failed for {}", request, error);
        processingErrorHandler.handleError(request, error);
    }

    private void handleProcessingSuccess(ActionItemAsyncRequest request) {
        log.info("Processing completed for {}", request);
        //offsetCommitter.commitOffset(request);
    }

}
