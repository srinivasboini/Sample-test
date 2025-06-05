package com.example.adapter.in.kafka.handler;

import com.example.adapter.in.kafka.ActionItemAsyncRequest;
import com.example.adapter.in.kafka.ActionItemAvroMapper;
import com.example.port.in.ProcessActionItemCommand;
import com.example.port.in.ReceiveActionItemUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Message processor responsible for handling action item messages.
 *
 * This component acts as the bridge between the Kafka consumer and the domain use cases.
 * It handles the conversion and processing of incoming messages while maintaining
 * separation of concerns according to clean architecture principles.
 *
 * Key responsibilities:
 * - Converts Avro messages to domain commands
 * - Delegates processing to appropriate use cases
 * - Maintains isolation between infrastructure and domain logic
 *
 * Processing Flow:
 * 1. Receives Avro message from Kafka consumer
 * 2. Converts message to domain command using mapper
 * 3. Delegates to domain use case for business logic processing
 *
 * Error Handling:
 * - Throws exceptions for invalid messages
 * - Lets caller handle retries and error management
 * - Maintains transaction boundaries
 *
 * @see com.example.adapter.in.kafka.ActionItemAvroMapper
 * @see com.example.port.in.ReceiveActionItemUseCase
 */
@Component
@Slf4j
@RequiredArgsConstructor
class ActionItemMessageProcessor {

    private final ReceiveActionItemUseCase receiveActionItemUseCase;
    private final ActionItemAvroMapper actionItemAvroMapper;

    /**
     * Processes a single action item message.
     *
     * @param actionItemAsyncRequest Kafka record containing the action item in Avro format
     * @throws IllegalArgumentException if message format is invalid
     * @throws RuntimeException if processing fails
     */
    public void process(ActionItemAsyncRequest actionItemAsyncRequest) {
        ProcessActionItemCommand command = actionItemAvroMapper.toCommand(actionItemAsyncRequest);
        receiveActionItemUseCase.processActionItem(command);
    }
}
