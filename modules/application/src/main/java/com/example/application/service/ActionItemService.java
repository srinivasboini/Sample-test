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
 * <p>
 * This service acts as a facade between the outer layers (adapters) and the domain layer,
 * coordinating the execution of business operations while maintaining transaction boundaries.
 *
 * <b>Responsibilities:</b>
 * <ul>
 *   <li><b>Transaction Management:</b> Ensures atomic operations and manages database consistency.</li>
 *   <li><b>Flow Orchestration:</b> Coordinates between adapters and domain, manages the sequence of operations, and handles cross-cutting concerns.</li>
 *   <li><b>Use Case Implementation:</b> Implements the ReceiveActionItemUseCase port, translates commands to domain operations, and coordinates persistence operations.</li>
 * </ul>
 *
 * <b>Flow Sequence:</b>
 * <ol>
 *   <li>Receive command from adapter</li>
 *   <li>Check if action item with uniqueId exists</li>
 *   <li>Build or update domain model</li>
 *   <li>Validate category-type combination exists in master configuration</li>
 *   <li>Validate through domain service</li>
 *   <li>Persist through output port</li>
 *   <li>Return result</li>
 * </ol>
 *
 * <b>Business Rules:</b>
 * <ul>
 *   <li>Action items can be opened (OPEN status) or closed (CLOSE status).</li>
 *   <li>Only one record per uniqueId should exist at any time.</li>
 *   <li>Closed items can be reopened by updating status to OPEN.</li>
 *   <li>Open items can be closed by updating status to CLOSE.</li>
 *   <li>If no record exists with uniqueId, creates new record with OPEN status only.</li>
 * </ul>
 *
 * <b>Validation Strategy:</b>
 * <ul>
 *   <li>Uses MasterConfigurationService.validateCategoryTypeCode() which leverages domain service for business rule validation, provides caching, ensures consistent validation logic, and includes proper logging and error handling.</li>
 * </ul>
 *
 * @see com.example.domain.service.ActionItemDomainService
 * @see com.example.port.out.SaveActionItemPort
 * @see com.example.port.in.ReceiveActionItemUseCase
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
     * <p>
     * Implements the business logic for action item lifecycle:
     * <ol>
     *   <li>Checks if action item with uniqueId exists</li>
     *   <li>Creates or updates domain model based on existence</li>
     *   <li>Validates category-type combination exists in master configuration</li>
     *   <li>Validates it through domain service</li>
     *   <li>Persists it through the output port</li>
     * </ol>
     *
     * <b>Business Logic:</b>
     * <ul>
     *   <li>If no record exists with uniqueId: creates new record (only with OPEN status)</li>
     *   <li>If record exists with uniqueId: updates existing record (can be OPEN or CLOSE)</li>
     *   <li>Ensures only one record per uniqueId exists at any time</li>
     * </ul>
     *
     * <b>Transaction Boundary:</b> Entire operation is atomic; rollback occurs if any step fails.
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
     * <p>
     * Copies over immutable fields and updates mutable fields from the command.
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
     * <p>
     * Delegates to MasterConfigurationService.validateCategoryTypeCode(), which uses the domain service for business rule validation, leverages caching, and provides consistent validation logic and error handling.
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
     * <p>
     * Generates a new unique ID and sets creation/update timestamps.
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
