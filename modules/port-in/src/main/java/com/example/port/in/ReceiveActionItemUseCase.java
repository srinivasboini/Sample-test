package com.example.port.in;

import com.example.domain.model.ActionItem;

/**
 * Input port for receiving and processing action items.
 *
 * This interface defines the primary use case for receiving action items into the system.
 * It acts as a boundary between the external adapters and the application core,
 * following the Ports & Adapters (Hexagonal) architecture pattern.
 *
 * Purpose:
 * - Defines the contract for processing incoming action items
 * - Isolates the application core from external concerns
 * - Enables multiple adapter implementations (Kafka, REST, etc.)
 *
 * Usage Example:
 * ```java
 * ProcessActionItemCommand command = ProcessActionItemCommand.builder()
 *     .title("Complete Documentation")
 *     .status("PENDING")
 *     .build();
 * ActionItem result = useCase.processActionItem(command);
 * ```
 *
 * Contract Guarantees:
 * - Thread-safe execution
 * - Transactional boundaries maintained
 * - Domain validation enforced
 *
 * @see ProcessActionItemCommand
 * @see ActionItem
 */
public interface ReceiveActionItemUseCase {

    /**
     * Processes an incoming action item command.
     *
     * @param command The command containing action item details
     * @return The processed action item with generated ID and metadata
     * @throws IllegalArgumentException if the command fails validation
     * @throws RuntimeException if processing fails due to system errors
     */
    ActionItem processActionItem(ProcessActionItemCommand command);
}
