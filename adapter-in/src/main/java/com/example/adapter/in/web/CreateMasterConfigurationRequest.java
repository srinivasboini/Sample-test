package com.example.adapter.in.web;

import lombok.Data;

@Data
public class CreateMasterConfigurationRequest {
    private String category;
    private String typeCode;
    private String description;
    private boolean active;
} 