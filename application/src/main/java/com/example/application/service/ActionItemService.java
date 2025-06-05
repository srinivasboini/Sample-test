package com.example.application.service;

import com.example.domain.model.ActionItem;
import com.example.domain.model.ActionItemStatus;
import com.example.domain.model.InvalidCategoryTypeException;
import com.example.domain.service.ActionItemDomainService;
import com.example.port.in.ProcessActionItemCommand;
import com.example.port.in.ReceiveActionItemUseCase;
import com.example.port.out.SaveActionItemPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
 * 2. Build domain model
 * 3. Validate category-type combination exists in master configuration
 * 4. Validate through domain service
 * 5. Persist through output port
 * 6. Return result
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
public class ActionItemService implements ReceiveActionItemUseCase {

    private final ActionItemDomainService domainService;
    private final SaveActionItemPort saveActionItemPort;
    private final MasterConfigurationService masterConfigurationService;

    /**
     * Processes an action item command by coordinating domain and persistence operations.
     *
     * This method:
     * 1. Creates a domain model from the command
     * 2. Validates category-type combination exists in master configuration
     * 3. Validates it through domain service
     * 4. Persists it through the output port
     *
     * Transaction Boundary:
     * - Entire operation is atomic
     * - Rollback occurs if any step fails
     *
     * @param command The command containing action item details
     * @return The processed and persisted action item
     * @throws InvalidCategoryTypeException if category-type combination is invalid
     * @throws IllegalArgumentException if validation fails
     * @throws RuntimeException if persistence fails
     */
    @Override
    @Transactional
    public ActionItem processActionItem(ProcessActionItemCommand command) {
        // Build domain model from command
        ActionItem actionItem = buildDomainModel(command);

        // Validate category and type code combination exists in master configuration
        validateCategoryTypeCodeCombination(actionItem.getCategory(), actionItem.getTypeCode());

        // Validate through domain service
        actionItem = domainService.validateAndEnrichActionItem(actionItem);

        // Persist through port
        return saveActionItemPort.saveActionItem(actionItem);
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
     * Builds a domain model from the incoming command.
     *
     * @param command Source command with action item details
     * @return New ActionItem domain model
     */
    private ActionItem buildDomainModel(ProcessActionItemCommand command) {
        return ActionItem.builder()
                .id(UUID.randomUUID().toString())
                .title(command.getTitle())
                .description(command.getDescription())
                .assignee(command.getAssignee())
                .category(command.getCategory())
                .typeCode(command.getTypeCode())
                .status(ActionItemStatus.valueOf(command.getStatus()))
                .dueDate(command.getDueDate())
                .createdAt(command.getCreatedAt())
                .updatedAt(command.getUpdatedAt())
                .build();
    }
}
