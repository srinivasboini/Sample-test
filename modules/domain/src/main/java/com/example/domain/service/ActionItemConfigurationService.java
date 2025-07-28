package com.example.domain.service;

import com.example.domain.model.ActionItemConfiguration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ActionItemConfigurationService {
    private final Map<String, ActionItemConfiguration> configurations = new ConcurrentHashMap<>();

    public ActionItemConfiguration loadConfiguration(ActionItemConfiguration config) {
        // Pure business logic - no logging or framework dependencies
        return config;
    }

    public ActionItemConfiguration getConfiguration(String category, String typeCode) {
        // Pure business logic - return null if not found
        return null;
    }

    public boolean isValidConfiguration(String category, String typeCode) {
        ActionItemConfiguration config = getConfiguration(category, typeCode);
        if (config == null) {
            return false;
        }
        return config.isActive();
    }

    public void clearCache() {
        // Pure business logic - clear the in-memory cache
        configurations.clear();
    }
} 