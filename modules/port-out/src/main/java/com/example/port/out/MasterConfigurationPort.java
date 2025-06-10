package com.example.port.out;

import com.example.domain.model.MasterConfiguration;

import java.util.List;
import java.util.Optional;

public interface MasterConfigurationPort {
    
    /**
     * Saves a master configuration entity
     * @param masterConfiguration the configuration to save
     * @return the saved configuration
     */
    MasterConfiguration save(MasterConfiguration masterConfiguration);
    
    /**
     * Saves multiple master configuration entities
     * @param masterConfigurations the list of configurations to save
     * @return the list of saved configurations
     */
    List<MasterConfiguration> saveAll(List<MasterConfiguration> masterConfigurations);
    
    /**
     * Finds all active master configurations
     * @return list of active configurations
     */
    List<MasterConfiguration> findAllActive();
    
    /**
     * Finds a master configuration by category and type code
     * @param category the category to search for
     * @param typeCode the type code to search for
     * @return optional containing the configuration if found
     */
    Optional<MasterConfiguration> findByCategoryAndTypeCode(String category, String typeCode);
    
    /**
     * Checks if a master configuration exists by category, type code and active status
     * @param category the category to check
     * @param typeCode the type code to check
     * @param active the active status to check
     * @return true if exists, false otherwise
     */
    boolean existsByCategoryAndTypeCodeAndActive(String category, String typeCode, boolean active);
}
