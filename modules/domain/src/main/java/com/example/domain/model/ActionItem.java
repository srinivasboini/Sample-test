package com.example.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Immutable domain model representing an Action Item.
 * <p>
 * This class encapsulates all the properties and state of an action item in the system.
 * It is constructed using the builder pattern for immutability and clarity.
 *
 * <b>Fields:</b>
 * <ul>
 *   <li><b>id:</b> Unique identifier for the action item (UUID string).</li>
 *   <li><b>uniqueId:</b> Business-level unique identifier (used for idempotency and lookup).</li>
 *   <li><b>title:</b> Short, descriptive title of the action item.</li>
 *   <li><b>description:</b> Detailed description of the action item.</li>
 *   <li><b>assignee:</b> User or entity responsible for the action item.</li>
 *   <li><b>category:</b> Category to which the action item belongs (e.g., "bug", "feature").</li>
 *   <li><b>typeCode:</b> Type code for further classification within a category.</li>
 *   <li><b>status:</b> Current status of the action item (OPEN or CLOSE).</li>
 *   <li><b>dueDate:</b> When the action item is expected to be completed.</li>
 *   <li><b>createdAt:</b> Timestamp when the action item was created.</li>
 *   <li><b>updatedAt:</b> Timestamp when the action item was last updated.</li>
 * </ul>
 *
 * <b>Immutability:</b> All fields are final and set via the builder. This ensures thread safety and consistency.
 *
 * @see ActionItemStatus
 */
@Value
@Builder
@Getter
public class ActionItem {
    /** Unique identifier for the action item (UUID string). */
    String id;
    /** Business-level unique identifier (used for idempotency and lookup). */
    String uniqueId;
    /** Short, descriptive title of the action item. */
    String title;
    /** Detailed description of the action item. */
    String description;
    /** User or entity responsible for the action item. */
    String assignee;
    /** Category to which the action item belongs (e.g., "bug", "feature"). */
    String category;
    /** Type code for further classification within a category. */
    String typeCode;
    /** Current status of the action item (OPEN or CLOSE). */
    ActionItemStatus status;
    /** When the action item is expected to be completed. */
    LocalDateTime dueDate;
    /** Timestamp when the action item was created. */
    LocalDateTime createdAt;
    /** Timestamp when the action item was last updated. */
    LocalDateTime updatedAt;
}
