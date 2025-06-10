package com.example.port.out;

import com.example.domain.model.ActionItem;

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
 *
 * Expected Behaviors:
 * - New items should be created with provided ID
 * - Existing items should be updated if ID exists
 * - Should throw appropriate exceptions for constraint violations
 * - Must maintain audit timestamps (createdAt, updatedAt)
 *
 * @see ActionItem
 */
public interface SaveActionItemPort {

    /**
     * Persists an action item to storage.
     *
     * @param actionItem The domain model to persist
     * @return The persisted action item, potentially with updated metadata
     * @throws RuntimeException if persistence fails
     */
    ActionItem saveActionItem(ActionItem actionItem);
}
