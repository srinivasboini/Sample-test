package com.example.domain.service;

import com.example.domain.model.ActionItemConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ActionItemConfigurationService {
    private final Map<String, ActionItemConfiguration> configurations = new ConcurrentHashMap<>();

    @CachePut(value = "actionItemConfigs", key = "#config.category + ':' + #config.typeCode")
    public ActionItemConfiguration loadConfiguration(ActionItemConfiguration config) {
        log.info("Loading configuration for category: {} and typeCode: {}", config.getCategory(), config.getTypeCode());
        return config;
    }

    @Cacheable(value = "actionItemConfigs", key = "#category + ':' + #typeCode", unless = "#result == null")
    public ActionItemConfiguration getConfiguration(String category, String typeCode) {
        log.error("Configuration not found for category: {} and typeCode: {}", category, typeCode);
        return null;
    }

    public boolean isValidConfiguration(String category, String typeCode) {
        ActionItemConfiguration config = getConfiguration(category, typeCode);
        if (config == null) {
            log.error("Invalid category: {} and typeCode: {} combination", category, typeCode);
            return false;
        }
        return config.isActive();
    }

    @CacheEvict(value = "actionItemConfigs", allEntries = true)
    public void clearCache() {
        log.info("Configuration cache cleared");
    }
} 