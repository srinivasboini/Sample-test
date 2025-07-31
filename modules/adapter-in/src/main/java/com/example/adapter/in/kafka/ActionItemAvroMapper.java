package com.example.adapter.in.kafka;

import com.example.avro.ActionItemAvro;
import com.example.avro.ActionItemStatusAvro;
import com.example.port.in.ProcessActionItemCommand;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Maps Avro-based ActionItem messages to domain commands for processing.
 * <p>
 * This component is responsible for converting Avro records received from Kafka into
 * domain-specific command objects that can be handled by the application layer. It ensures
 * that all relevant fields are mapped and that date/time and status conversions are handled
 * appropriately.
 * <p>
 * <b>Responsibilities:</b>
 * <ul>
 *   <li>Extracts and maps fields from Avro messages to domain commands</li>
 *   <li>Handles status and date/time conversions</li>
 *   <li>Provides default values for missing or null fields</li>
 * </ul>
 * <b>Usage:</b> Used by Kafka consumers and message processors to translate incoming Avro messages
 * into actionable domain commands.
 */
@Component
public class ActionItemAvroMapper {

    public ProcessActionItemCommand toCommand(ActionItemAsyncRequest actionItemAsyncRequest) {
        ActionItemAvro avro = actionItemAsyncRequest.getConsumerRecord().value();

        return ProcessActionItemCommand.builder()
                .uniqueId(avro.getUniqueId())
                .title(avro.getTitle())
                .description(avro.getDescription())
                .assignee(avro.getAssignee())
                .category(avro.getCategory())
                .typeCode(avro.getTypeCode())
                .status(mapStatus(avro.getStatus()))
                .dueDate(toLocalDateTime(avro.getDueDate()))
                .createdAt(toLocalDateTime(avro.getCreatedAt()))
                .updatedAt(toLocalDateTime(avro.getUpdatedAt()))
                .build();
    }

    private String mapStatus(ActionItemStatusAvro status) {
        return status != null ? status.name() : "PENDING";
    }

    private LocalDateTime toLocalDateTime(Instant timestamp) {
        return LocalDateTime.ofInstant(timestamp,
                ZoneId.systemDefault()
        );
    }
}

