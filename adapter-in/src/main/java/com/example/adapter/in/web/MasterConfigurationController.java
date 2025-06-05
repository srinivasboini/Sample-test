package com.example.adapter.in.web;

import com.example.domain.model.MasterConfiguration;
import com.example.port.in.CreateMasterConfigurationCommand;
import com.example.port.in.MasterConfigurationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/master-configurations")
@RequiredArgsConstructor
@Slf4j
public class MasterConfigurationController {
    
    private final MasterConfigurationUseCase masterConfigurationUseCase;
    
    /**
     * Get all active master configurations
     */
    @GetMapping
    public ResponseEntity<List<MasterConfiguration>> getAllActiveConfigurations() {
        log.info("Fetching all active master configurations");
        List<MasterConfiguration> configurations = masterConfigurationUseCase.getAllActiveMasterConfigurations();
        return ResponseEntity.ok(configurations);
    }
    
    /**
     * Validate a category-type combination
     */
    @GetMapping("/validate")
    public ResponseEntity<String> validateCategoryTypeCode(
            @RequestParam String category,
            @RequestParam String typeCode) {
        
        log.info("Validating category: {} and typeCode: {}", category, typeCode);
        
        boolean isActive = masterConfigurationUseCase.isMasterConfigurationActive(category, typeCode);
        if (isActive) {
            return ResponseEntity.ok("Valid category-type combination");
        } else {
            log.warn("Invalid category-type combination: category={}, typeCode={}", category, typeCode);
            return ResponseEntity.badRequest().body("Invalid category-type combination");
        }
    }
    
    /**
     * Create a single master configuration
     */
    @PostMapping
    public ResponseEntity<MasterConfiguration> createMasterConfiguration(
            @RequestBody CreateMasterConfigurationRequest request) {
        
        log.info("Creating master configuration for category: {} and typeCode: {}", 
                request.getCategory(), request.getTypeCode());
        
        CreateMasterConfigurationCommand command = CreateMasterConfigurationCommand.builder()
                .category(request.getCategory())
                .typeCode(request.getTypeCode())
                .description(request.getDescription())
                .active(request.isActive())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        MasterConfiguration savedConfiguration = masterConfigurationUseCase.createMasterConfiguration(command);
        return ResponseEntity.ok(savedConfiguration);
    }
    
    /**
     * Create multiple master configurations
     */
    @PostMapping("/batch")
    public ResponseEntity<List<MasterConfiguration>> createMasterConfigurations(
            @RequestBody List<CreateMasterConfigurationRequest> requests) {
        
        log.info("Creating {} master configurations", requests.size());
        
        List<CreateMasterConfigurationCommand> commands = requests.stream()
                .map(request -> CreateMasterConfigurationCommand.builder()
                        .category(request.getCategory())
                        .typeCode(request.getTypeCode())
                        .description(request.getDescription())
                        .active(request.isActive())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());
        
        List<MasterConfiguration> savedConfigurations = masterConfigurationUseCase.createMasterConfigurations(commands);
        return ResponseEntity.ok(savedConfigurations);
    }
    
    /**
     * Find master configuration by category and type code
     */
    @GetMapping("/find")
    public ResponseEntity<MasterConfiguration> findMasterConfiguration(
            @RequestParam String category,
            @RequestParam String typeCode) {
        
        log.info("Finding master configuration for category: {} and typeCode: {}", category, typeCode);
        
        return masterConfigurationUseCase.findMasterConfiguration(category, typeCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Debug endpoint to test the isMasterConfigurationActive method
     */
    @GetMapping("/debug")
    public ResponseEntity<String> debugCategoryTypeCode(
            @RequestParam String category,
            @RequestParam String typeCode) {
        
        try {
            log.info("DEBUG: Testing category: {} and typeCode: {}", category, typeCode);
            
            boolean isActive = masterConfigurationUseCase.isMasterConfigurationActive(category, typeCode);
            log.info("DEBUG: Result for category: {} and typeCode: {} is: {}", category, typeCode, isActive);
            
            return ResponseEntity.ok("Result: " + isActive);
        } catch (Exception e) {
            log.error("DEBUG: Error testing category: {} and typeCode: {}", category, typeCode, e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
} 