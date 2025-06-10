package com.example.application.service;

import com.example.domain.model.ProcessingError;
import com.example.port.in.HandleProcessingErrorUseCase;
import com.example.port.out.PersistErrorPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service that implements error handling use cases.
 *
 * This service acts as a facade between the outer layers (adapters) and the domain layer,
 * coordinating error handling operations while maintaining transaction boundaries.
 *
 * Responsibilities:
 * 1. Transaction Management
 *    - Ensures atomic error persistence operations
 *    - Manages database consistency
 *
 * 2. Error Handling Orchestration
 *    - Coordinates between adapters and error persistence
 *    - Manages the sequence of error handling operations
 *    - Handles cross-cutting concerns
 *
 * 3. Use Case Implementation
 *    - Implements the HandleProcessingErrorUseCase port
 *    - Translates error information to domain model
 *    - Coordinates persistence operations
 */
@Service
@RequiredArgsConstructor
public class ErrorHandlingService implements HandleProcessingErrorUseCase {

    private final PersistErrorPort persistErrorPort;

    /**
     * Handles a processing error by persisting it to the error storage.
     *
     * This method:
     * 1. Creates a domain model from the error information
     * 2. Persists it through the output port
     *
     * Transaction Boundary:
     * - Entire operation is atomic
     * - Rollback occurs if persistence fails
     *
     * @param source The source of the error (e.g., "KAFKA_CONSUMER")
     * @param error The throwable that caused the error
     * @param payload The context/payload where the error occurred
     */
    @Override
    @Transactional
    public void handleError(String source, Throwable error, String payload) {
        ProcessingError processingError = ProcessingError.builder()
                .source(source)
                .errorType(error.getClass().getName())
                .errorMessage(error.getMessage())
                .stackTrace(getStackTraceAsString(error))
                .payload(payload)
                .occurredAt(java.time.LocalDateTime.now())
                .status("ERROR")
                .build();
                
        persistErrorPort.persistError(processingError);
    }
    
    private String getStackTraceAsString(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
} 