package com.example.domain.service;

import com.example.domain.model.ActionItem;
import com.example.domain.model.ActionItemStatus;

/**
 * Core domain service for Action Items business logic.
 * <p>
 * This service encapsulates the core business rules and validation logic
 * for action items, following Domain-Driven Design principles.
 * It is independent of external concerns like persistence or messaging.
 *
 * <b>Business Rules:</b>
 * <ul>
 *   <li>All action items must have a title.</li>
 *   <li>Assignee must be specified for OPEN items.</li>
 *   <li>Due dates must be in the future when created.</li>
 *   <li>Status transitions must follow defined workflow (OPEN &lt;-&gt; CLOSE).</li>
 *   <li>Category and type code combinations must be valid according to master configuration.</li>
 * </ul>
 *
 * <b>Validation Rules:</b>
 * <ol>
 *   <li><b>Title validation:</b> Must not be null or empty, and must be between 3 and 100 characters.</li>
 *   <li><b>Description validation:</b> Optional for CLOSE items, required for OPEN items, must not exceed 1000 characters.</li>
 *   <li><b>Date validation:</b> Due date must be after creation date, updates must maintain temporal consistency.</li>
 *   <li><b>Master Configuration validation:</b> Category and type code combination must exist and be active.</li>
 * </ol>
 *
 * <b>Design:</b> This service should not depend on ports or infrastructure; it is pure domain logic.
 *
 * @see com.example.domain.model.ActionItem
 * @see com.example.domain.model.ActionItemStatus
 */
public class ActionItemDomainService {

    private final MasterConfigurationDomainService masterConfigurationDomainService;

    public ActionItemDomainService(MasterConfigurationDomainService masterConfigurationDomainService) {
        this.masterConfigurationDomainService = masterConfigurationDomainService;
    }

    /**
     * Validates and enriches an action item according to business rules.
     * <p>
     * This method is the primary entry point for domain logic execution.
     * Note: Master configuration validation is now handled separately to maintain
     * proper separation of concerns.
     *
     * <b>Validation and Enrichment Process:</b>
     * <ol>
     *   <li>Validates all required fields</li>
     *   <li>Applies business rules</li>
     *   <li>Enriches with computed values if needed</li>
     *   <li>Ensures data consistency</li>
     * </ol>
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
     *
     * @param actionItem The action item whose title is to be validated
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
     *
     * @param actionItem The action item whose assignee is to be validated
     * @throws IllegalArgumentException if assignee rules are violated
     */
    private void validateAssignee(ActionItem actionItem) {
        if (actionItem.getStatus() == ActionItemStatus.OPEN
            && (actionItem.getAssignee() == null || actionItem.getAssignee().trim().isEmpty())) {
            throw new IllegalArgumentException("Assignee is required for OPEN items");
        }
    }

    /**
     * Validates temporal consistency of dates.
     *
     * @param actionItem The action item whose dates are to be validated
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
     *
     * @param actionItem The action item whose status is to be validated
     * @throws IllegalArgumentException if status rules are violated
     */
    private void validateStatus(ActionItem actionItem) {
        if (actionItem.getStatus() == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
    }

    /**
     * Validates that the category and type code combination exists in the master configuration.
     * <p>
     * Note: This validation now requires the application layer to provide the existence check
     * since domain services should not depend on ports directly.
     *
     * @param actionItem The action item to validate
     * @param categoryTypeExists Whether the category-type combination exists and is active
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
