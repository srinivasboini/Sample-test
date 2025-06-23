package com.example.port.in;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Command object representing an action item processing request.
 *
 * This class serves as a Data Transfer Object (DTO) for incoming action item requests,
 * following the Command pattern in clean architecture. It encapsulates all the data
 * needed to process an action item while maintaining isolation between layers.
 *
 * Usage:
 * - Used by external adapters to communicate with the application core
 * - Immutable by design (using @Value)
 * - Built using the Builder pattern for convenience
 *
 * Data Validation:
 * - All fields are validated at the application service level
 * - Domain-specific validation is handled separately
 * - Structural validation can be added via Bean Validation if needed
 *
 * @see ReceiveActionItemUseCase
 */
@Value
@Builder
public class ProcessActionItemCommand {
    /** Unique identifier for the action item business entity */
    String uniqueId;

    /** Unique title identifying the action item */
    String title;

    /** Detailed description of the action item */
    String description;

    /** Person or team assigned to the action item */
    String assignee;

    /** Category of the action item */
    String category;

    /** Type code of the action item */
    String typeCode;

    /** Current status of the action item */
    String status;

    /** When the action item is due */
    LocalDateTime dueDate;

    /** When the action item was created */
    LocalDateTime createdAt;

    /** When the action item was last updated */
    LocalDateTime updatedAt;
}
