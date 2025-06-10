package com.example.application.config;

import com.example.application.service.MasterConfigurationService;
import com.example.domain.model.MasterConfiguration;
import com.example.port.in.CreateMasterConfigurationCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MasterConfigurationLoader implements ApplicationRunner {
    
    private final MasterConfigurationService masterConfigurationService;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Loading master configurations on application startup...");
        
        try {
            // Clear any existing cache entries first to ensure clean state
            masterConfigurationService.clearAllCaches();
            log.info("Cleared all caches before loading configurations");
            
            // Check if configurations already exist
            List<MasterConfiguration> existingConfigs = masterConfigurationService.getAllActiveMasterConfigurations();
            
            if (existingConfigs.isEmpty()) {
                log.info("No existing master configurations found. Loading default configurations...");
                loadDefaultConfigurations();
            } else {
                log.info("Found {} existing master configurations. Skipping default load.", existingConfigs.size());
            }
            
            // Clear cache again after loading to ensure fresh cache
            masterConfigurationService.clearAllCaches();
            
            // Load configurations into cache
            masterConfigurationService.getAllActiveMasterConfigurations();
            log.info("Master configurations loaded and cached successfully.");
            
        } catch (Exception e) {
            log.error("Failed to load master configurations on startup", e);
            throw e;
        }
    }
    
    private void loadDefaultConfigurations() {
        LocalDateTime now = LocalDateTime.now();
        
        List<CreateMasterConfigurationCommand> defaultConfigurations = Arrays.asList(
                CreateMasterConfigurationCommand.builder()
                .category("TASK")
                .typeCode("URGENT")
                .description("Urgent task category")
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build(),
                CreateMasterConfigurationCommand.builder()
                .category("TASK")
                .typeCode("NORMAL")
                .description("Normal task category")
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build(),
                CreateMasterConfigurationCommand.builder()
                .category("TASK")
                .typeCode("LOW")
                .description("Low priority task category")
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build(),
                CreateMasterConfigurationCommand.builder()
                .category("PROJECT")
                .typeCode("DEVELOPMENT")
                .description("Development project category")
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build(),
                CreateMasterConfigurationCommand.builder()
                .category("PROJECT")
                .typeCode("MAINTENANCE")
                .description("Maintenance project category")
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build(),
                CreateMasterConfigurationCommand.builder()
                .category("ISSUE")
                .typeCode("BUG")
                .description("Bug issue category")
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build(),
                CreateMasterConfigurationCommand.builder()
                .category("ISSUE")
                .typeCode("ENHANCEMENT")
                .description("Enhancement issue category")
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build()
        );
        
        List<MasterConfiguration> savedConfigurations = masterConfigurationService.createMasterConfigurations(defaultConfigurations);
        log.info("Loaded {} default master configurations", savedConfigurations.size());
    }
} 