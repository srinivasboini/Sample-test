package com.example.adapter.in.kafka.config;

import com.example.adapter.in.kafka.interceptor.MdcKafkaInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration class for MDC (Mapped Diagnostic Context) integration with Kafka.
 * 
 * This configuration sets up the MDC interceptor with Kafka consumer factories
 * and provides beans for MDC management across the application.
 * 
 * Features:
 * - Configures MDC interceptor with consumer factories
 * - Provides MDC context management beans
 * - Supports correlation ID propagation
 * - Thread-safe MDC operations
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MdcKafkaConfig {

    private final MdcKafkaInterceptor mdcKafkaInterceptor;

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:action-item-consumer-group}")
    private String groupId;

    @Value("${spring.kafka.consumer.auto-offset-reset:earliest}")
    private String autoOffsetReset;

    @Value("${spring.kafka.consumer.key-deserializer:org.apache.kafka.common.serialization.StringDeserializer}")
    private String keyDeserializer;

    @Value("${spring.kafka.consumer.value-deserializer:io.confluent.kafka.serializers.KafkaAvroDeserializer}")
    private String valueDeserializer;

    @Value("${spring.kafka.properties.schema.registry.url:http://localhost:8081}")
    private String schemaRegistryUrl;

    /**
     * Creates a consumer factory with MDC interceptor configured.
     * 
     * @return ConsumerFactory with MDC interceptor
     */
    @Bean
    public ConsumerFactory<String, Object> mdcConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        
        // Basic Kafka configuration
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);
        
        // Schema registry configuration
        props.put("schema.registry.url", schemaRegistryUrl);
        props.put("specific.avro.reader", true);
        
        // MDC interceptor configuration
        props.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, 
                 MdcKafkaInterceptor.class.getName());
        
        // Additional consumer properties for better performance
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1);
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);
        
        log.info("Created MDC-enabled consumer factory with bootstrap servers: {}, group: {}", 
                bootstrapServers, groupId);
        
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Creates a consumer factory specifically for Avro messages with MDC interceptor.
     * 
     * @return ConsumerFactory for Avro messages with MDC interceptor
     */
    @Bean("avroConsumerFactory")
    public ConsumerFactory<String, Object> avroConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        
        // Basic Kafka configuration
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);
        
        // Schema registry configuration for Avro
        props.put("schema.registry.url", schemaRegistryUrl);
        props.put("specific.avro.reader", true);
        props.put("auto.register.schemas", false);
        
        // MDC interceptor configuration
        props.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, 
                 MdcKafkaInterceptor.class.getName());
        
        // Consumer properties optimized for Avro
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1);
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);
        
        log.info("Created Avro MDC-enabled consumer factory with schema registry: {}", schemaRegistryUrl);
        
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Provides the MDC interceptor bean for manual configuration.
     * 
     * @return MdcKafkaInterceptor instance
     */
    @Bean
    public MdcKafkaInterceptor mdcKafkaInterceptor() {
        log.info("Providing MDC Kafka interceptor bean");
        return mdcKafkaInterceptor;
    }

    /**
     * Creates a map of consumer properties with MDC interceptor for manual configuration.
     * 
     * @return Map of consumer properties with MDC interceptor
     */
    @Bean("mdcConsumerProperties")
    public Map<String, Object> mdcConsumerProperties() {
        Map<String, Object> props = new HashMap<>();
        
        // Basic configuration
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);
        
        // Schema registry
        props.put("schema.registry.url", schemaRegistryUrl);
        props.put("specific.avro.reader", true);
        
        // MDC interceptor
        props.put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, 
                 MdcKafkaInterceptor.class.getName());
        
        // Performance tuning
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1);
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);
        
        log.info("Created MDC consumer properties map");
        
        return props;
    }
} 