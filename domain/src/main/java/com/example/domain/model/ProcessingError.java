package com.example.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class ProcessingError {
    private final String id;
    private final String source;
    private final String errorType;
    private final String errorMessage;
    private final String stackTrace;
    private final String payload;
    private final LocalDateTime occurredAt;
    private final String status;
} 