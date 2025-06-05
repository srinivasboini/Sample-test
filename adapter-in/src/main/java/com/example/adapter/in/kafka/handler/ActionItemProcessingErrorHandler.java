package com.example.adapter.in.kafka.handler;

import com.example.adapter.in.kafka.ActionItemAsyncRequest;
import com.example.port.in.HandleProcessingErrorUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
class ActionItemProcessingErrorHandler {

    private final HandleProcessingErrorUseCase handleProcessingErrorUseCase;

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
