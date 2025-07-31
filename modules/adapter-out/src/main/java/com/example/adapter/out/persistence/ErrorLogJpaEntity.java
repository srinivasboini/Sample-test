package com.example.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JPA entity representing error logs stored in the database.
 * <p>
 * This entity captures error messages, stack traces, error codes, timestamps, and source information
 * for persistent error tracking and analysis.
 * <p>
 * <b>Responsibilities:</b>
 * <ul>
 *   <li>Stores error log details for troubleshooting and auditing</li>
 *   <li>Supports persistence and retrieval via JPA repositories</li>
 * </ul>
 * <b>Usage:</b> Used by error handling and logging components to persist error information.
 */
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
