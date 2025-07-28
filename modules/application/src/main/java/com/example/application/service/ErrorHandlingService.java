package com.example.application.service;

import com.example.domain.model.ProcessingError;
import com.example.port.in.HandleProcessingErrorUseCase;
import com.example.port.out.PersistErrorPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service that implements error handling use cases for the application.
 * <p>
 * This service acts as a facade between the outer layers (adapters) and the domain layer,
 * coordinating error handling operations while maintaining transaction boundaries.
 *
 * <b>Responsibilities:</b>
 * <ul>
 *   <li><b>Transaction Management:</b> Ensures atomic error persistence operations and manages database consistency.</li>
 *   <li><b>Error Handling Orchestration:</b> Coordinates between adapters and error persistence, manages the sequence of error handling operations, and handles cross-cutting concerns.</li>
 *   <li><b>Use Case Implementation:</b> Implements the HandleProcessingErrorUseCase port, translates error information to domain model, and coordinates persistence operations.</li>
 * </ul>
 *
 * <b>Flow Sequence:</b>
 * <ol>
 *   <li>Receive error details from adapter or service</li>
 *   <li>Build domain model for the error</li>
 *   <li>Persist error through output port</li>
 * </ol>
 *
 * <b>Transaction Boundary:</b> Entire operation is atomic; rollback occurs if persistence fails.
 *
 * @see com.example.domain.model.ProcessingError
 * @see com.example.port.out.PersistErrorPort
 * @see com.example.port.in.HandleProcessingErrorUseCase
 */
@Service
@RequiredArgsConstructor
public class ErrorHandlingService implements HandleProcessingErrorUseCase {

    private final PersistErrorPort persistErrorPort;

    /**
     * Handles a processing error by persisting it to the error storage.
     * <p>
     * This method:
     * <ol>
     *   <li>Creates a domain model from the error information</li>
     *   <li>Persists it through the output port</li>
     * </ol>
     *
     * <b>Transaction Boundary:</b> Entire operation is atomic; rollback occurs if persistence fails.
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
    
    /**
     * Utility method to convert a stack trace to a string for logging and persistence.
     *
     * @param throwable The throwable whose stack trace is to be converted
     * @return The stack trace as a string
     */
    private String getStackTraceAsString(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
} 