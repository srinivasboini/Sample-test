package com.example.domain.service;

import com.example.domain.model.InvalidCategoryTypeException;
import com.example.domain.model.MasterConfiguration;

import java.util.List;

public class MasterConfigurationDomainService {
    
    /**
     * Validate if a category-type combination is valid and active
     * This method validates business rules for master configurations
     */
    public void validateCategoryTypeCode(String category, String typeCode, boolean exists) {
        if (!exists) {
            throw new InvalidCategoryTypeException(category, typeCode);
        }
    }
    
    /**
     * Validate master configuration business rules
     */
    public void validateMasterConfiguration(MasterConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("Master configuration cannot be null");
        }
        
        if (configuration.getCategory() == null || configuration.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be null or empty");
        }
        
        if (configuration.getTypeCode() == null || configuration.getTypeCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Type code cannot be null or empty");
        }
        
        if (configuration.getDescription() == null || configuration.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }
    }
    
    /**
     * Validate a list of master configurations
     */
    public void validateMasterConfigurations(List<MasterConfiguration> configurations) {
        if (configurations == null || configurations.isEmpty()) {
            throw new IllegalArgumentException("Master configurations list cannot be null or empty");
        }
        
        configurations.forEach(this::validateMasterConfiguration);
    }
    
    /**
     * Enrich master configuration with business logic
     */
    public MasterConfiguration enrichMasterConfiguration(MasterConfiguration configuration) {
        // Apply any business enrichment logic here
        return configuration;
    }
} 