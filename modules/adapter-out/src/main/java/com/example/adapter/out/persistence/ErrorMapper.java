package com.example.adapter.out.persistence;

import com.example.domain.model.ProcessingError;
import org.springframework.stereotype.Component;

/**
 * Maps between ProcessingError domain models and ErrorEntity JPA entities for persistence operations.
 * <p>
 * This component is responsible for converting ProcessingError objects between their domain
 * representation and their database (JPA entity) representation. It ensures that all
 * relevant fields are mapped correctly for both persistence and retrieval.
 * <p>
 * <b>Responsibilities:</b>
 * <ul>
 *   <li>Converts ProcessingError domain objects to ErrorEntity JPA entities for storage</li>
 *   <li>Converts ErrorEntity JPA entities to ProcessingError domain objects for business logic</li>
 *   <li>Ensures field consistency and handles mapping logic</li>
 * </ul>
 * <b>Usage:</b> Used by error persistence adapters and repositories to translate between domain and entity layers.
 */
@Component
public class ErrorMapper {
    
    public ErrorEntity toEntity(ProcessingError error) {
        ErrorEntity entity = new ErrorEntity();
        entity.setSource(error.getSource());
        entity.setErrorType(error.getErrorType());
        entity.setErrorMessage(error.getErrorMessage());
        entity.setStackTrace(error.getStackTrace());
        entity.setPayload(error.getPayload());
        entity.setOccurredAt(error.getOccurredAt());
        entity.setStatus(error.getStatus());
        return entity;
    }
    
    public ProcessingError toDomain(ErrorEntity entity) {
        return ProcessingError.builder()
                .id(entity.getId())
                .source(entity.getSource())
                .errorType(entity.getErrorType())
                .errorMessage(entity.getErrorMessage())
                .stackTrace(entity.getStackTrace())
                .payload(entity.getPayload())
                .occurredAt(entity.getOccurredAt())
                .status(entity.getStatus())
                .build();
    }
} 