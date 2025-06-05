package com.example.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
@Getter
public class MasterConfiguration {
    String id;
    String category;
    String typeCode;
    String description;
    boolean active;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
} 