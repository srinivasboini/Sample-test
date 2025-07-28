package com.example.application.service;

import com.example.domain.model.MasterConfiguration;
import com.example.domain.service.MasterConfigurationDomainService;
import com.example.port.in.CreateMasterConfigurationCommand;
import com.example.port.in.MasterConfigurationUseCase;
import com.example.port.out.MasterConfigurationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application service that orchestrates master configuration operations.
 * <p>
 * This service acts as a facade between the outer layers (adapters) and the domain layer,
 * coordinating the execution of master configuration business operations while maintaining 
 * transaction boundaries.
 *
 * <b>Responsibilities:</b>
 * <ul>
 *   <li><b>Transaction Management:</b> Ensures atomic operations and manages database consistency.</li>
 *   <li><b>Flow Orchestration:</b> Coordinates between adapters and domain, manages the sequence of operations, and handles cross-cutting concerns.</li>
 *   <li><b>Use Case Implementation:</b> Implements the MasterConfigurationUseCase port, translates commands to domain operations, and coordinates persistence operations.</li>
 * </ul>
 *
 * <b>Flow Sequence:</b>
 * <ol>
 *   <li>Receive command from adapter</li>
 *   <li>Build domain model</li>
 *   <li>Validate through domain service</li>
 *   <li>Persist through output port</li>
 *   <li>Return result</li>
 * </ol>
 *
 * <b>Validation Strategy:</b>
 * <ul>
 *   <li>Uses domain service for business rule validation, provides caching, ensures consistent validation logic, and includes proper logging and error handling.</li>
 * </ul>
 *
 * @see com.example.port.out.MasterConfigurationPort
 * @see com.example.port.in.MasterConfigurationUseCase
 * @see com.example.domain.service.MasterConfigurationDomainService
 */
@Service
@RequiredArgsConstructor
public class MasterConfigurationService implements MasterConfigurationUseCase {

    private final MasterConfigurationPort masterConfigurationPort;
    private final MasterConfigurationDomainService domainService;

    /**
     * Creates a new master configuration by coordinating domain and persistence operations.
     * <p>
     * This method:
     * <ol>
     *   <li>Creates a domain model from the command</li>
     *   <li>Validates through domain service</li>
     *   <li>Checks for duplicates</li>
     *   <li>Persists it through the output port</li>
     * </ol>
     *
     * <b>Transaction Boundary:</b> Entire operation is atomic; rollback occurs if any step fails.
     *
     * @param command The command containing master configuration details
     * @return The created and persisted master configuration
     * @throws IllegalArgumentException if validation fails
     * @throws RuntimeException if persistence fails
     */
    @Override
    @Transactional
    @CacheEvict(value = {"categoryTypeValidation", "masterConfigurations"}, allEntries = true)
    public MasterConfiguration createMasterConfiguration(CreateMasterConfigurationCommand command) {
        // Build domain model from command
        MasterConfiguration masterConfiguration = buildDomainModel(command);
        
        // Validate through domain service
        domainService.validateMasterConfiguration(masterConfiguration);
        
        // Check for duplicates
        validateNoDuplicateActiveConfiguration(masterConfiguration);
        
        // Enrich through domain service
        masterConfiguration = domainService.enrichMasterConfiguration(masterConfiguration);
        
        // Persist through port
        return masterConfigurationPort.save(masterConfiguration);
    }

    /**
     * Creates multiple master configurations in a single transaction.
     * <p>
     * This method processes a batch of commands, validates, checks for duplicates, enriches, and persists them.
     *
     * @param commands List of commands containing master configuration details
     * @return List of created and persisted master configurations
     */
    @Override
    @Transactional
    @CacheEvict(value = {"categoryTypeValidation", "masterConfigurations"}, allEntries = true)
    public List<MasterConfiguration> createMasterConfigurations(List<CreateMasterConfigurationCommand> commands) {
        List<MasterConfiguration> masterConfigurations = commands.stream()
                .map(this::buildDomainModel)
                .collect(Collectors.toList());
        
        // Validate through domain service
        domainService.validateMasterConfigurations(masterConfigurations);
        
        // Check for duplicates
        masterConfigurations.forEach(this::validateNoDuplicateActiveConfiguration);
        
        // Enrich through domain service
        masterConfigurations = masterConfigurations.stream()
                .map(domainService::enrichMasterConfiguration)
                .collect(Collectors.toList());
        
        return masterConfigurationPort.saveAll(masterConfigurations);
    }

    /**
     * Retrieves all active master configurations.
     * <p>
     * This method is cached to improve performance.
     *
     * @return List of active master configurations
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "masterConfigurations")
    public List<MasterConfiguration> getAllActiveMasterConfigurations() {
        return masterConfigurationPort.findAllActive();
    }

    /**
     * Finds a master configuration by category and type code.
     *
     * @param category The category to search for
     * @param typeCode The type code to search for
     * @return Optional containing the master configuration if found
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<MasterConfiguration> findMasterConfiguration(String category, String typeCode) {
        return masterConfigurationPort.findByCategoryAndTypeCode(category, typeCode);
    }

    /**
     * Checks if a master configuration exists and is active.
     * <p>
     * This method is cached to improve performance for validation.
     *
     * @param category The category to check
     * @param typeCode The type code to check
     * @return true if the configuration exists and is active, false otherwise
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "categoryTypeValidation", key = "#category + '_' + #typeCode")
    public boolean isMasterConfigurationActive(String category, String typeCode) {
        return masterConfigurationPort.existsByCategoryAndTypeCodeAndActive(category, typeCode, true);
    }

    /**
     * Validates a category-type combination using domain service.
     * <p>
     * This method is cached to improve performance and delegates to the domain service for business rule validation.
     *
     * @param category The category to validate
     * @param typeCode The type code to validate
     * @throws com.example.domain.model.InvalidCategoryTypeException if the combination is invalid
     */
    @Cacheable(value = "categoryTypeValidation", key = "#category + '_' + #typeCode")
    public void validateCategoryTypeCode(String category, String typeCode) {
        boolean exists = masterConfigurationPort.existsByCategoryAndTypeCodeAndActive(category, typeCode, true);
        domainService.validateCategoryTypeCode(category, typeCode, exists);
    }

    /**
     * Builds a domain model from the incoming command.
     * <p>
     * Generates a new unique ID and sets creation/update timestamps.
     *
     * @param command Source command with master configuration details
     * @return New MasterConfiguration domain model
     */
    private MasterConfiguration buildDomainModel(CreateMasterConfigurationCommand command) {
        return MasterConfiguration.builder()
                .id(UUID.randomUUID().toString())
                .category(command.getCategory())
                .typeCode(command.getTypeCode())
                .description(command.getDescription())
                .active(command.isActive())
                .createdAt(command.getCreatedAt())
                .updatedAt(command.getUpdatedAt())
                .build();
    }

    /**
     * Validates that no duplicate active configuration exists.
     * <p>
     * Throws an exception if a duplicate is found.
     *
     * @param masterConfiguration The master configuration to validate
     * @throws IllegalArgumentException if a duplicate active configuration exists
     */
    private void validateNoDuplicateActiveConfiguration(MasterConfiguration masterConfiguration) {
        if (masterConfiguration.isActive() && 
            masterConfigurationPort.existsByCategoryAndTypeCodeAndActive(
                masterConfiguration.getCategory(), 
                masterConfiguration.getTypeCode(), 
                true)) {
            throw new IllegalArgumentException(
                String.format("Active master configuration already exists for category '%s' and type code '%s'", 
                    masterConfiguration.getCategory(), masterConfiguration.getTypeCode()));
        }
    }

    /**
     * Manually clears all master configuration caches.
     * <p>
     * This method is useful for troubleshooting cache-related issues.
     */
    @CacheEvict(value = {"categoryTypeValidation", "masterConfigurations"}, allEntries = true)
    public void clearAllCaches() {
        // Cache will be cleared by the annotation
    }
} 