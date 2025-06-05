package com.example.port.in;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
@Getter
public class CreateMasterConfigurationCommand {
    String category;
    String typeCode;
    String description;
    boolean active;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
} 