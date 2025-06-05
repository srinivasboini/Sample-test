package com.example.adapter.in.kafka;

import com.example.avro.ActionItemAvro;
import com.example.avro.ActionItemStatusAvro;
import com.example.port.in.ProcessActionItemCommand;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class ActionItemAvroMapper {

    public ProcessActionItemCommand toCommand(ActionItemAsyncRequest actionItemAsyncRequest) {
        ActionItemAvro avro = actionItemAsyncRequest.getConsumerRecord().value();

        return ProcessActionItemCommand.builder()
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

