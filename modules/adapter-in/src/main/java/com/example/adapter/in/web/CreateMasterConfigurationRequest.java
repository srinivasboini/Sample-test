package com.example.adapter.in.web;

import lombok.Data;

/**
 * Request DTO for creating a new master configuration via the API.
 * <p>
 * Encapsulates the data required to create a master configuration, including category,
 * type code, description, and active status. Used by controller endpoints to receive
 * and validate incoming requests from clients.
 * <p>
 * <b>Responsibilities:</b>
 * <ul>
 *   <li>Holds request data for master configuration creation</li>
 *   <li>Supports validation and mapping to domain commands</li>
 * </ul>
 * <b>Usage:</b> Used as the request body in REST endpoints for creating or updating
 * master configuration records.
 */
@Data
public class CreateMasterConfigurationRequest {
    private String category;
    private String typeCode;
    private String description;
    private boolean active;
} 