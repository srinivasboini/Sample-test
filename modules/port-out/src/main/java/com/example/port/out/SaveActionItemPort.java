package com.example.port.out;

import com.example.domain.model.ActionItem;
import com.example.domain.model.ActionItemStatus;
import com.example.domain.model.TypeCodeCount;
import java.util.List;
import java.util.Optional;

/**
 * Output port for persisting action items.
 *
 * This interface defines the contract for saving action items to persistent storage.
 * It follows the Port & Adapters pattern, allowing the application core to remain
 * independent of specific storage implementations.
 *
 * Storage Agnostic:
 * - Could be implemented by any storage mechanism (JPA, MongoDB, etc.)
 * - Domain model remains pure and unaware of persistence details
 * - Enables easy switching between storage implementations
 *
 * Implementation Requirements:
 * 1. Must handle unique ID constraints
 * 2. Must maintain data consistency
 * 3. Must handle concurrent modifications
 * 4. Must preserve all domain model attributes
 * 5. Must enforce uniqueId business rule (only one record per uniqueId)
 *
 * Expected Behaviors:
 * - New items should be created with provided ID and uniqueId
 * - Existing items should be updated if uniqueId exists
 * - Should throw appropriate exceptions for constraint violations
 * - Must maintain audit timestamps (createdAt, updatedAt)
 * - Must ensure only one record exists per uniqueId at any time
 *
 * Business Rules:
 * - Action items can be opened (OPEN status) or closed (CLOSE status)
 * - Only one record per uniqueId should exist at any time
 * - Closed items can be reopened by updating status to OPEN
 * - Open items can be closed by updating status to CLOSE
 *
 * @see ActionItem
 */
public interface SaveActionItemPort {

    /**
     * Persists an action item to storage.
     *
     * Business Logic:
     * - If no record exists with the uniqueId, creates a new record
     * - If a record exists with the uniqueId, updates the existing record
     * - Ensures only one record per uniqueId exists at any time
     *
     * @param actionItem The domain model to persist
     * @return The persisted action item, potentially with updated metadata
     * @throws RuntimeException if persistence fails
     */
    ActionItem saveActionItem(ActionItem actionItem);

    /**
     * Finds an action item by its business unique identifier.
     *
     * @param uniqueId the business unique identifier
     * @return Optional containing the action item if found
     */
    Optional<ActionItem> findByUniqueId(String uniqueId);

    /**
     * Get list of typeCodes with their counts filtered by status.
     * 
     * @param status the ActionItemStatus to filter by
     * @return List of TypeCodeCount containing typeCode and count
     */
    List<TypeCodeCount> getTypeCodesByCountAndStatus(ActionItemStatus status);
}
