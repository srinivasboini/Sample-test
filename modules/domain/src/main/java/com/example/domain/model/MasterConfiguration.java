package com.example.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

import java.time.LocalDateTime;

/**
 * Immutable domain model representing a Master Configuration.
 * <p>
 * This class encapsulates the properties and state of a master configuration entry in the system.
 * It is constructed using the builder pattern for immutability and clarity.
 *
 * <b>Fields:</b>
 * <ul>
 *   <li><b>id:</b> Unique identifier for the master configuration (UUID string).</li>
 *   <li><b>category:</b> Category to which this configuration applies.</li>
 *   <li><b>typeCode:</b> Type code for further classification within a category.</li>
 *   <li><b>description:</b> Human-readable description of the configuration.</li>
 *   <li><b>active:</b> Whether this configuration is currently active and valid for use.</li>
 *   <li><b>createdAt:</b> Timestamp when the configuration was created.</li>
 *   <li><b>updatedAt:</b> Timestamp when the configuration was last updated.</li>
 * </ul>
 *
 * <b>Immutability:</b> All fields are final and set via the builder. This ensures thread safety and consistency.
 */
@Value
@Builder
@Getter
public class MasterConfiguration {
    /** Unique identifier for the master configuration (UUID string). */
    String id;
    /** Category to which this configuration applies. */
    String category;
    /** Type code for further classification within a category. */
    String typeCode;
    /** Human-readable description of the configuration. */
    String description;
    /** Whether this configuration is currently active and valid for use. */
    boolean active;
    /** Timestamp when the configuration was created. */
    LocalDateTime createdAt;
    /** Timestamp when the configuration was last updated. */
    LocalDateTime updatedAt;
} 