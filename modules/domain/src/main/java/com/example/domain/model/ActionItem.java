package com.example.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
@Getter
public class ActionItem {
    String id;
    String title;
    String description;
    String assignee;
    String category;
    String typeCode;
    ActionItemStatus status;
    LocalDateTime dueDate;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
