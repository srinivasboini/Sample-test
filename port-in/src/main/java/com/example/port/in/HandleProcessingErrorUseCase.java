package com.example.port.in;

/**
 * Use case for handling processing errors in the system.
 * 
 * This port defines the contract for error handling operations,
 * allowing adapters to report errors while maintaining
 * separation of concerns.
 */
public interface HandleProcessingErrorUseCase {
    
    /**
     * Handles a processing error by persisting it to the error storage.
     *
     * @param source The source of the error (e.g., "KAFKA_CONSUMER")
     * @param error The throwable that caused the error
     * @param payload The context/payload where the error occurred
     */
    void handleError(String source, Throwable error, String payload);
} 