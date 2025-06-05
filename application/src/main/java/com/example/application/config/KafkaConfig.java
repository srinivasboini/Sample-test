package com.example.application.config;

import com.example.avro.ActionItemAvro;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Configuration for Action Items processing.
 *
 * This configuration class sets up the Kafka consumer infrastructure with:
 * - Avro deserialization support
 * - Manual acknowledgment
 * - Error handling
 * - Retry policies
 * - Thread pool management
 *
 * Key Features:
 * 1. Consumer Configuration
 *    - Manual offset management
 *    - Avro message format support
 *    - Schema Registry integration
 *
 * 2. Error Handling
 *    - Retry with backoff
 *    - Dead letter handling
 *    - Error deserialization
 *
 * 3. Performance Tuning
 *    - Configurable concurrency
 *    - Optimized fetch settings
 *    - Session management
 *
 * Important Settings:
 * - Bootstrap Servers: Kafka cluster connection
 * - Schema Registry: Avro schema management
 * - Group ID: Consumer group management
 * - Session Timeout: Consumer health monitoring
 * - Poll Settings: Message batch processing
 */
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.properties.schema.registry.url}")
    private String schemaRegistryUrl;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    /**
     * Creates a Kafka consumer factory with Avro deserialization support.
     * Configures essential Kafka consumer properties including:
     * - Deserialization
     * - Error handling
     * - Consumer behavior settings
     * - Schema registry integration
     *
     * @return ConsumerFactory configured for Avro messages
     */
    @Bean
    public ConsumerFactory<String, ActionItemAvro> consumerFactory() {
        Map<String, Object> props = new HashMap<>();

        // Basic Kafka Configuration
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        // Deserializer Configuration
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, KafkaAvroDeserializer.class);

        // Schema Registry Configuration
        props.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);

        // Consumer Behavior Configuration
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000);

        // Performance Tuning
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1);
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);
        
        // Connection management to prevent infinite retries
        props.put(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, 30000);
        props.put(ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, 300000);
        props.put(ConsumerConfig.RETRY_BACKOFF_MS_CONFIG, 5000);
        
        // Limit connection timeouts and idle connections
        props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        props.put(ConsumerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, 540000);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Creates a Kafka listener container factory with error handling and manual acks.
     *
     * Features:
     * - Manual acknowledgment mode
     * - Concurrent message processing
     * - Retry policy with backoff
     * - Error handling strategy
     *
     * @return ConcurrentKafkaListenerContainerFactory for processing Avro messages
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ActionItemAvro> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ActionItemAvro> factory =
            new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());

        // Configure Manual Acknowledgment
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        // Configure Concurrency
        factory.setConcurrency(3);

        // Configure Error Handler with retry policy
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
            new FixedBackOff(5000L, 3L)
        );
        factory.setCommonErrorHandler(errorHandler);
       
        return factory;
    }
}
