package com.example.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

@Value
@Builder
@Getter
public class ActionItemConfiguration {
    String id;
    String category;
    String typeCode;
    String description;
    boolean active;
    String metaId;
    String createdBy;
    String updatedBy;
} 