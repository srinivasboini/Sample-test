package com.example.port.out;

/**
 * Port interface for handling @Run annotation operations
 * This port defines the contract for what should happen when the @Run annotation is triggered
 */
public interface RunPort {
    /**
     * Executes the run operation with the given message
     * @param message the message associated with the run operation
     * @param methodName the name of the method that was annotated
     * @param className the name of the class containing the annotated method
     */
    void executeRun(String message, String methodName, String className);
} 