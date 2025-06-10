package com.example.port.in;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class CreateActionItemCommand {
    String title;
    String description;
    String assignee;
    String category;
    String typeCode;
    String status;
    LocalDateTime dueDate;
}
