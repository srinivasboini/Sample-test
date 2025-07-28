package com.example.domain.model;

/**
 * Exception thrown when an invalid category-type combination is encountered.
 * <p>
 * This exception is used to signal that a given category and type code do not form a valid or active combination
 * according to the master configuration. It is typically thrown during validation in the domain or application layer.
 *
 * Usage scenarios:
 * <ul>
 *   <li>When a category-type pair does not exist in the master configuration.</li>
 *   <li>When a category-type pair is inactive or otherwise invalid.</li>
 * </ul>
 *
 * Provides multiple constructors for flexibility in error reporting.
 */
public class InvalidCategoryTypeException extends RuntimeException {
    /**
     * Constructs an exception with a formatted message for the invalid category-type combination.
     *
     * @param category the invalid category
     * @param typeCode the invalid type code
     */
    public InvalidCategoryTypeException(String category, String typeCode) {
        super(String.format("Invalid category-type combination: category='%s', typeCode='%s'", category, typeCode));
    }
    /**
     * Constructs an exception with a custom message.
     *
     * @param message the detail message
     */
    public InvalidCategoryTypeException(String message) {
        super(message);
    }
    /**
     * Constructs an exception with a custom message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public InvalidCategoryTypeException(String message, Throwable cause) {
        super(message, cause);
    }
} 