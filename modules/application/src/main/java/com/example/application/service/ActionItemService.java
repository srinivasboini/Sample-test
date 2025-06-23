package com.example.application.service;

import com.example.domain.model.ActionItem;
import com.example.domain.model.ActionItemStatus;
import com.example.domain.model.InvalidCategoryTypeException;
import com.example.domain.service.ActionItemDomainService;
import com.example.port.in.ProcessActionItemCommand;
import com.example.port.in.ReceiveActionItemUseCase;
import com.example.port.out.SaveActionItemPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Application service that orchestrates the flow of action item processing.
 *
 * This service acts as a facade between the outer layers (adapters) and the domain layer,
 * coordinating the execution of business operations while maintaining transaction boundaries.
 *
 * Responsibilities:
 * 1. Transaction Management
 *    - Ensures atomic operations
 *    - Manages database consistency
 *
 * 2. Flow Orchestration
 *    - Coordinates between adapters and domain
 *    - Manages the sequence of operations
 *    - Handles cross-cutting concerns
 *
 * 3. Use Case Implementation
 *    - Implements the ReceiveActionItemUseCase port
 *    - Translates commands to domain operations
 *    - Coordinates persistence operations
 *
 * Flow Sequence:
 * 1. Receive command from adapter
 * 2. Check if action item with uniqueId exists
 * 3. Build or update domain model
 * 4. Validate category-type combination exists in master configuration
 * 5. Validate through domain service
 * 6. Persist through output port
 * 7. Return result
 *
 * Business Rules:
 * - Action items can be opened (OPEN status) or closed (CLOSE status)
 * - Only one record per uniqueId should exist at any time
 * - Closed items can be reopened by updating status to OPEN
 * - Open items can be closed by updating status to CLOSE
 * - If no record exists with uniqueId, creates new record with OPEN status only
 *
 * Validation Strategy:
 * - Uses MasterConfigurationService.validateCategoryTypeCode() which:
 *   - Leverages domain service for business rule validation
 *   - Provides caching for improved performance
 *   - Ensures consistent validation logic across the application
 *   - Includes proper logging and error handling
 *
 * @see ActionItemDomainService
 * @see SaveActionItemPort
 * @see ReceiveActionItemUseCase
 * @see MasterConfigurationService#validateCategoryTypeCode(String, String)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ActionItemService implements ReceiveActionItemUseCase {

    private final ActionItemDomainService domainService;
    private final SaveActionItemPort saveActionItemPort;
    private final MasterConfigurationService masterConfigurationService;

    /**
     * Processes an action item command by coordinating domain and persistence operations.
     *
     * This method implements the business logic for action item lifecycle:
     * 1. Checks if action item with uniqueId exists
     * 2. Creates or updates domain model based on existence
     * 3. Validates category-type combination exists in master configuration
     * 4. Validates it through domain service
     * 5. Persists it through the output port
     *
     * Business Logic:
     * - If no record exists with uniqueId: creates new record (only with OPEN status)
     * - If record exists with uniqueId: updates existing record (can be OPEN or CLOSE)
     * - Ensures only one record per uniqueId exists at any time
     *
     * Transaction Boundary:
     * - Entire operation is atomic
     * - Rollback occurs if any step fails
     *
     * @param command The command containing action item details
     * @return The processed and persisted action item
     * @throws InvalidCategoryTypeException if category-type combination is invalid
     * @throws IllegalArgumentException if validation fails or business rules are violated
     * @throws RuntimeException if persistence fails
     */
    @Override
    @Transactional
    public ActionItem processActionItem(ProcessActionItemCommand command) {
        log.info("Processing action item with uniqueId: {}, status: {}", command.getUniqueId(), command.getStatus());
        
        // Validate uniqueId is provided
        if (command.getUniqueId() == null || command.getUniqueId().trim().isEmpty()) {
            throw new IllegalArgumentException("UniqueId is required for action item processing");
        }

        // Check if action item with this uniqueId already exists
        var existingActionItem = saveActionItemPort.findByUniqueId(command.getUniqueId());
        
        ActionItem actionItem;
        if (existingActionItem.isPresent()) {
            // Update existing action item
            log.debug("Found existing action item with uniqueId: {}, updating", command.getUniqueId());
            actionItem = updateExistingActionItem(existingActionItem.get(), command);
        } else {
            // Create new action item
            log.debug("No existing action item found with uniqueId: {}, creating new", command.getUniqueId());
            
            // Validate that new items can only be created with OPEN status
            if (!ActionItemStatus.OPEN.name().equals(command.getStatus())) {
                throw new IllegalArgumentException("New action items can only be created with OPEN status. Current status: " + command.getStatus());
            }
            
            actionItem = buildNewDomainModel(command);
        }

        // Validate category and type code combination exists in master configuration
        validateCategoryTypeCodeCombination(actionItem.getCategory(), actionItem.getTypeCode());

        // Validate through domain service
        actionItem = domainService.validateAndEnrichActionItem(actionItem);

        // Persist through port
        return saveActionItemPort.saveActionItem(actionItem);
    }

    /**
     * Updates an existing action item with new command data.
     * 
     * @param existing The existing action item
     * @param command The command with updated data
     * @return Updated action item domain model
     */
    private ActionItem updateExistingActionItem(ActionItem existing, ProcessActionItemCommand command) {
        return ActionItem.builder()
                .id(existing.getId()) // Keep existing ID
                .uniqueId(existing.getUniqueId()) // Keep existing uniqueId
                .title(command.getTitle())
                .description(command.getDescription())
                .assignee(command.getAssignee())
                .category(command.getCategory())
                .typeCode(command.getTypeCode())
                .status(ActionItemStatus.valueOf(command.getStatus()))
                .dueDate(command.getDueDate())
                .createdAt(existing.getCreatedAt()) // Keep original creation time
                .updatedAt(LocalDateTime.now()) // Update the updated timestamp
                .build();
    }

    /**
     * Validates that the category and type code combination exists in the master configuration.
     * 
     * This method delegates to MasterConfigurationService.validateCategoryTypeCode() which:
     * - Uses the domain service for proper business rule validation
     * - Leverages caching for improved performance 
     * - Provides consistent validation logic across the application
     * - Includes proper logging and detailed error messages
     * - Handles all edge cases and business rules centrally
     *
     * @param category The category to validate
     * @param typeCode The type code to validate
     * @throws InvalidCategoryTypeException if the combination doesn't exist, is inactive, or validation fails
     */
    private void validateCategoryTypeCodeCombination(String category, String typeCode) {
        if (category == null || typeCode == null) {
            throw new InvalidCategoryTypeException("Category and type code cannot be null");
        }
        
        // Use the proper validation method that leverages domain service and caching
        masterConfigurationService.validateCategoryTypeCode(category, typeCode);
    }

    /**
     * Builds a new domain model from the incoming command.
     *
     * @param command Source command with action item details
     * @return New ActionItem domain model
     */
    private ActionItem buildNewDomainModel(ProcessActionItemCommand command) {
        return ActionItem.builder()
                .id(UUID.randomUUID().toString())
                .uniqueId(command.getUniqueId())
                .title(command.getTitle())
                .description(command.getDescription())
                .assignee(command.getAssignee())
                .category(command.getCategory())
                .typeCode(command.getTypeCode())
                .status(ActionItemStatus.valueOf(command.getStatus()))
                .dueDate(command.getDueDate())
                .createdAt(command.getCreatedAt() != null ? command.getCreatedAt() : LocalDateTime.now())
                .updatedAt(command.getUpdatedAt() != null ? command.getUpdatedAt() : LocalDateTime.now())
                .build();
    }
}
