package com.example.adapter.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Column;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import com.example.domain.model.ActionItemStatus;

/**
 * Persistence entity for Action Items in the database.
 * This class represents the database structure for storing action items and
 * follows Jakarta Persistence (JPA) specifications.
 *
 * Key features:
 * - Maps to 'action_items' table in the database
 * - Uses String-based UUID as primary key
 * - Stores temporal data using LocalDateTime
 * - Uses enumerated type for status
 * - Enforces unique constraint on uniqueId to ensure only one record per business entity
 *
 * Database Schema:
 * - id: Primary key (UUID as String)
 * - uniqueId: Business unique identifier (unique constraint)
 * - title: Task title
 * - description: Detailed task description
 * - assignee: Person responsible for the task
 * - category: Category of the action item
 * - typeCode: Type code of the action item
 * - status: Current state of the task (OPEN, CLOSE)
 * - dueDate: Deadline for task completion
 * - createdAt: Timestamp of creation
 * - updatedAt: Timestamp of last update
 *
 * @see com.example.domain.model.ActionItemStatus
 */
@Entity
@Table(name = "action_items", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"uniqueId"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionItemJpaEntity {

    /** Unique identifier for the action item */
    @Id
    private String id;

    /** Business unique identifier for the action item */
    @Column(name = "uniqueId", nullable = false)
    private String uniqueId;

    /** Title of the action item - brief description */
    private String title;

    /** Detailed description of what needs to be done */
    private String description;

    /** Person or team assigned to complete this item */
    private String assignee;

    /** Category of the action item */
    private String category;

    /** Type code of the action item */
    private String typeCode;

    /**
     * Current status of the action item
     * Stored as STRING in database for better readability and maintenance
     */
    @Enumerated(EnumType.STRING)
    private ActionItemStatus status;

    /** Deadline for completing this action item */
    private LocalDateTime dueDate;

    /** Timestamp when this action item was created */
    private LocalDateTime createdAt;

    /** Timestamp of the last update to this action item */
    private LocalDateTime updatedAt;
}
