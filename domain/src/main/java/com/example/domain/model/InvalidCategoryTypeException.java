package com.example.domain.model;

public class InvalidCategoryTypeException extends RuntimeException {
    
    public InvalidCategoryTypeException(String category, String typeCode) {
        super(String.format("Invalid category-type combination: category='%s', typeCode='%s'", category, typeCode));
    }
    
    public InvalidCategoryTypeException(String message) {
        super(message);
    }
    
    public InvalidCategoryTypeException(String message, Throwable cause) {
        super(message, cause);
    }
} 