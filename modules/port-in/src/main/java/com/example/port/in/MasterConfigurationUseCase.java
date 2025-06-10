package com.example.port.in;

import com.example.domain.model.MasterConfiguration;

import java.util.List;
import java.util.Optional;

public interface MasterConfigurationUseCase {
    
    /**
     * Create a new master configuration
     */
    MasterConfiguration createMasterConfiguration(CreateMasterConfigurationCommand command);
    
    /**
     * Create multiple master configurations
     */
    List<MasterConfiguration> createMasterConfigurations(List<CreateMasterConfigurationCommand> commands);
    
    /**
     * Retrieve all active master configurations
     */
    List<MasterConfiguration> getAllActiveMasterConfigurations();
    
    /**
     * Find master configuration by category and type code
     */
    Optional<MasterConfiguration> findMasterConfiguration(String category, String typeCode);
    
    /**
     * Check if a master configuration exists and is active
     */
    boolean isMasterConfigurationActive(String category, String typeCode);
} 