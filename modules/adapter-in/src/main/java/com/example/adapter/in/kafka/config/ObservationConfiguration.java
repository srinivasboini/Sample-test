package com.example.adapter.in.kafka.config;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for enabling Micrometer observation for Kafka operations.
 * This enables monitoring and tracing of Kafka message consumption.
 */
@Slf4j
@Configuration
public class ObservationConfiguration {

    /**
     * Creates the ObservedAspect bean that enables @Observed annotations.
     * This bean is required for @Observed annotations to function properly.
     */
    @Bean
    public ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
        log.info("Configuring ObservedAspect for Kafka observation support");
        return new ObservedAspect(observationRegistry);
    }
} 