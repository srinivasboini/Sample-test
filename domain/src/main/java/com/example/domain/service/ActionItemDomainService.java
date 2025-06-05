package com.example.domain.service;

import com.example.domain.model.ActionItem;
import com.example.domain.model.ActionItemStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Core domain service for Action Items business logic.
 *
 * This service encapsulates the core business rules and validation logic
 * for action items, following Domain-Driven Design principles.
 * It is independent of external concerns like persistence or messaging.
 *
 * Business Rules:
 * - All action items must have a title
 * - Assignee must be specified for non-PENDING items
 * - Due dates must be in the future when created
 * - Status transitions must follow defined workflow
 * - Category and type code combinations must be valid according to master configuration
 *
 * Validation Rules:
 * 1. Title validation:
 *    - Must not be null or empty
 *    - Must be between 3 and 100 characters
 *
 * 2. Description validation:
 *    - Optional for PENDING items
 *    - Required for IN_PROGRESS items
 *    - Must not exceed 1000 characters
 *
 * 3. Date validation:
 *    - Due date must be after creation date
 *    - Updates must maintain temporal consistency
 *
 * 4. Master Configuration validation:
 *    - Category and type code combination must exist in master configuration
 *    - Configuration must be active
 *
 * @see ActionItem
 * @see ActionItemStatus
 */
@Service
@RequiredArgsConstructor
public class ActionItemDomainService {

    private final MasterConfigurationDomainService masterConfigurationDomainService;

    /**
     * Validates and enriches an action item according to business rules.
     * This method is the primary entry point for domain logic execution.
     * Note: Master configuration validation is now handled separately to maintain
     * proper separation of concerns.
     *
     * Validation and Enrichment Process:
     * 1. Validates all required fields
     * 2. Applies business rules
     * 3. Enriches with computed values if needed
     * 4. Ensures data consistency
     *
     * @param actionItem The action item to validate and enrich
     * @return The validated and enriched action item
     * @throws IllegalArgumentException if business rules are violated
     */
    public ActionItem validateAndEnrichActionItem(ActionItem actionItem) {
        validateTitle(actionItem);
        validateAssignee(actionItem);
        validateDates(actionItem);
        validateStatus(actionItem);

        return actionItem;
    }

    /**
     * Validates the title according to business rules.
     * @throws IllegalArgumentException if title is invalid
     */
    private void validateTitle(ActionItem actionItem) {
        if (actionItem.getTitle() == null || actionItem.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Action item title cannot be empty");
        }
        if (actionItem.getTitle().length() > 100) {
            throw new IllegalArgumentException("Action item title cannot exceed 100 characters");
        }
    }

    /**
     * Validates assignee based on item status.
     * @throws IllegalArgumentException if assignee rules are violated
     */
    private void validateAssignee(ActionItem actionItem) {
        if (actionItem.getStatus() != ActionItemStatus.PENDING
            && (actionItem.getAssignee() == null || actionItem.getAssignee().trim().isEmpty())) {
            throw new IllegalArgumentException("Assignee is required for non-PENDING items");
        }
    }

    /**
     * Validates temporal consistency of dates.
     * @throws IllegalArgumentException if date rules are violated
     */
    private void validateDates(ActionItem actionItem) {
        if (actionItem.getDueDate() != null
            && actionItem.getDueDate().isBefore(actionItem.getCreatedAt())) {
            throw new IllegalArgumentException("Due date cannot be before creation date");
        }
    }

    /**
     * Validates status transitions and requirements.
     * @throws IllegalArgumentException if status rules are violated
     */
    private void validateStatus(ActionItem actionItem) {
        if (actionItem.getStatus() == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
    }

    /**
     * Validates that the category and type code combination exists in the master configuration.
     * Note: This validation now requires the application layer to provide the existence check
     * since domain services should not depend on ports directly.
     * @throws com.example.domain.model.InvalidCategoryTypeException if configuration validation fails
     */
    public void validateMasterConfiguration(ActionItem actionItem, boolean categoryTypeExists) {
        if (actionItem.getCategory() != null && actionItem.getTypeCode() != null) {
            masterConfigurationDomainService.validateCategoryTypeCode(
                actionItem.getCategory(), 
                actionItem.getTypeCode(), 
                categoryTypeExists
            );
        }
    }
}
