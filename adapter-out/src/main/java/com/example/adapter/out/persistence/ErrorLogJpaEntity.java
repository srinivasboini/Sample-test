package com.example.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "error_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorLogJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String stackTrace;

    private String errorCode;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private String source;
}
