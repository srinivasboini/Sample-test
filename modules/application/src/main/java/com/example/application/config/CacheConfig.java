package com.example.application.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuration class for application-level caching and JPA auditing.
 * <p>
 * Sets up cache manager beans and enables caching and JPA auditing for the application.
 * <p>
 * <b>Responsibilities:</b>
 * <ul>
 *   <li>Configures cache manager with named caches for validation and master configurations</li>
 *   <li>Enables Spring's caching and JPA auditing features</li>
 * </ul>
 * <b>Usage:</b> Used by services and repositories to cache frequently accessed data and audit entity changes.
 */
@Configuration
@EnableCaching
@EnableJpaAuditing
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(java.util.Arrays.asList(
            "categoryTypeValidation",
            "masterConfigurations"
        ));
        return cacheManager;
    }
} 