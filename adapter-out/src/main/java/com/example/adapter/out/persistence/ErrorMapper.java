package com.example.adapter.out.persistence;

import com.example.domain.model.ProcessingError;
import org.springframework.stereotype.Component;

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