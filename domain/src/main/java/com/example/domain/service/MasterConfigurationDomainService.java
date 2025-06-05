package com.example.domain.service;

import com.example.domain.model.InvalidCategoryTypeException;
import com.example.domain.model.MasterConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MasterConfigurationDomainService {
    
    /**
     * Validate if a category-type combination is valid and active
     * This method validates business rules for master configurations
     */
    public void validateCategoryTypeCode(String category, String typeCode, boolean exists) {
        log.debug("Validating category-type combination: category={}, typeCode={}", category, typeCode);
        
        if (!exists) {
            log.warn("Invalid category-type combination: category={}, typeCode={}", category, typeCode);
            throw new InvalidCategoryTypeException(category, typeCode);
        }
        
        log.debug("Category-type combination is valid: category={}, typeCode={}", category, typeCode);
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
        
        log.debug("Master configuration validation passed for category={}, typeCode={}", 
                 configuration.getCategory(), configuration.getTypeCode());
    }
    
    /**
     * Validate a list of master configurations
     */
    public void validateMasterConfigurations(List<MasterConfiguration> configurations) {
        if (configurations == null || configurations.isEmpty()) {
            throw new IllegalArgumentException("Master configurations list cannot be null or empty");
        }
        
        configurations.forEach(this::validateMasterConfiguration);
        
        log.info("Validated {} master configurations", configurations.size());
    }
    
    /**
     * Enrich master configuration with business logic
     */
    public MasterConfiguration enrichMasterConfiguration(MasterConfiguration configuration) {
        // Apply any business enrichment logic here
        log.debug("Enriching master configuration for category={}, typeCode={}", 
                 configuration.getCategory(), configuration.getTypeCode());
        
        return configuration;
    }
} 