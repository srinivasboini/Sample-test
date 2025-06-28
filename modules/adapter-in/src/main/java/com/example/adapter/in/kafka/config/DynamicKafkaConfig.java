package com.example.adapter.in.kafka.config;

import com.example.adapter.in.kafka.ActionItemAsyncRequest;
import com.example.adapter.in.kafka.ActionItemAsyncRequestProvider;
import com.example.adapter.in.kafka.ActionItemKafkaConsumer;
import com.example.adapter.in.kafka.handler.ActionItemAsyncMessageHandler;
import com.example.adapter.in.kafka.handler.MessageHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListenerConfigurer;
import org.springframework.kafka.config.MethodKafkaListenerEndpoint;
import org.springframework.kafka.config.KafkaListenerEndpointRegistrar;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.beans.factory.annotation.Value;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;

import lombok.extern.slf4j.Slf4j;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

// Additional imports for container factory access
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Field;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListenerContainer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DynamicKafkaConfig implements KafkaListenerConfigurer {

    private final MessageHandler<ActionItemAsyncRequest> messageHandler;
    private final ActionItemAsyncRequestProvider actionItemAsyncRequestProvider;
    private final MdcKafkaConfig mdcKafkaConfig;
    
    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public MessageHandlerMethodFactory messageHandlerMethodFactory() {
        DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
        factory.setValidator(new LocalValidatorFactoryBean());
        return factory;
    }

    /**
     * Creates a container factory with MDC interceptor support.
     * 
     * @return ConcurrentKafkaListenerContainerFactory with MDC support
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> mdcKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        
        // Use the MDC-enabled consumer factory
        factory.setConsumerFactory(mdcKafkaConfig.mdcConsumerFactory());
        
        // Configure container properties
        ContainerProperties containerProperties = factory.getContainerProperties();
        containerProperties.setAckMode(ContainerProperties.AckMode.MANUAL);
        containerProperties.setObservationEnabled(true);
        
        // Configure concurrency
        factory.setConcurrency(3);
        
        // Configure error handler
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
            new FixedBackOff(5000L, 3L)
        );
        factory.setCommonErrorHandler(errorHandler);
        
        log.info("Created MDC-enabled Kafka listener container factory");
        
        return factory;
    }
    

    
    /**
     * Logs all container properties for inspection
     */
    private void logContainerProperties(ContainerProperties containerProperties, String containerName) {
        log.info("=== Container Properties for {} ===", containerName);
        
        try {
            // Use reflection to access all properties
            Field[] fields = ContainerProperties.class.getDeclaredFields();
            
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(containerProperties);
                
                if (value != null) {
                    log.info("Property: {} = {}", field.getName(), value);
                }
            }
            
            // Log specific important properties
            log.info("Ack Mode: {}", containerProperties.getAckMode());
            log.info("Observation Enabled: {}", containerProperties.isObservationEnabled());
            log.info("Consumer Group ID: {}", containerProperties.getGroupId());
            log.info("Topic Pattern: {}", containerProperties.getTopicPattern());
            
        } catch (Exception e) {
            log.error("Error accessing container properties", e);
        }
        
        log.info("=== End Container Properties ===");
    }
    
    /**
     * Access and log all container properties from the application context
     */
    public void logAllContainerProperties() {
        log.info("=== Accessing All Kafka Container Properties ===");
        
        try {
            // Get the container factory
            ConcurrentKafkaListenerContainerFactory<String, Object> factory = 
                applicationContext.getBean(ConcurrentKafkaListenerContainerFactory.class);
            
            if (factory != null) {
                ContainerProperties containerProperties = factory.getContainerProperties();
                logContainerProperties(containerProperties, "Application Container Factory");
                
                // Log factory-specific properties
                log.info("Factory Batch Listener: {}", factory.isBatchListener());
                
                // Get consumer factory properties
                ConsumerFactory<?, ?> consumerFactory = factory.getConsumerFactory();
                if (consumerFactory instanceof DefaultKafkaConsumerFactory) {
                    DefaultKafkaConsumerFactory<?, ?> defaultFactory = 
                        (DefaultKafkaConsumerFactory<?, ?>) consumerFactory;
                    
                    Map<String, Object> configs = defaultFactory.getConfigurationProperties();
                    log.info("Consumer Factory Configuration Properties: {}", configs);
                }
            }
            
            // List all Kafka-related beans
            String[] kafkaBeanNames = applicationContext.getBeanNamesForType(
                ConcurrentKafkaListenerContainerFactory.class);
            
            log.info("Found {} Kafka listener container factories", kafkaBeanNames.length);
            for (String beanName : kafkaBeanNames) {
                log.info("Kafka Container Factory Bean: {}", beanName);
            }
            
        } catch (Exception e) {
            log.error("Error accessing container properties from application context", e);
        }
        
        log.info("=== End All Container Properties ===");
    }

    @Override
    public void configureKafkaListeners(KafkaListenerEndpointRegistrar registrar) {
        List<String> topics = List.of("action-items-topic-1", "action-items-topic-2", "action-items-topic-3");
        log.info("Configuring dynamic Kafka listeners for topics: {}", topics);
        
        // Log all container properties before configuring listeners
        logAllContainerProperties();
        
        for (String topic : topics) {
            try {
                registerKafkaListener(registrar, topic);
                log.info("Successfully registered Kafka listener for topic: {} ", topic);
            } catch (Exception e) {
                log.error("Failed to register Kafka listener for topic: {}", topic, e);
                throw new RuntimeException("Failed to configure Kafka listener for topic: " + topic, e);
            }
        }
    }

    private void registerKafkaListener(KafkaListenerEndpointRegistrar registrar, String topic)
            throws NoSuchMethodException {

        MethodKafkaListenerEndpoint<String, Object> endpoint = new MethodKafkaListenerEndpoint<>();

        // Basic endpoint configuration
        endpoint.setId(UUID.randomUUID().toString());
        endpoint.setTopics(topic);
        endpoint.setGroupId("group-"+topic);
        endpoint.setBean(new ActionItemKafkaConsumer(messageHandler, actionItemAsyncRequestProvider));

        // Set the consume method from ActionItemKafkaConsumer
        Method consumeMethod = ActionItemKafkaConsumer.class.getMethod("consume", ConsumerRecord.class, Acknowledgment.class);
        endpoint.setMethod(consumeMethod);

        // Set message handler method factory
        endpoint.setMessageHandlerMethodFactory(messageHandlerMethodFactory());

        // Log endpoint properties
        log.info("Endpoint Configuration for topic {}: ID={}, GroupId={}, Concurrency={}, AutoStartup={}", 
                topic, endpoint.getId(), endpoint.getGroupId(), endpoint.getConcurrency(), endpoint.getAutoStartup());

        registrar.registerEndpoint(endpoint);
    }
}
