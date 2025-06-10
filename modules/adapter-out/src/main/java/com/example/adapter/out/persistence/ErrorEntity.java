package com.example.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "processing_errors")
@Getter
@Setter
@NoArgsConstructor
public class ErrorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String source;
    
    @Column(name = "error_type", nullable = false)
    private String errorType;
    
    @Column(name = "error_message", nullable = false)
    private String errorMessage;
    
    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;
    
    @Column(columnDefinition = "TEXT")
    private String payload;
    
    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;
    
    @Column(nullable = false)
    private String status;
} 