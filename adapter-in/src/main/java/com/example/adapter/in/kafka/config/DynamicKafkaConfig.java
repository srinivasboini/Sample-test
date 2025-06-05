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

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DynamicKafkaConfig implements KafkaListenerConfigurer {

    private final MessageHandler<ActionItemAsyncRequest> messageHandler;
    private final ActionItemAsyncRequestProvider actionItemAsyncRequestProvider;
    

    @Bean
    public MessageHandlerMethodFactory messageHandlerMethodFactory() {
        DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
        factory.setValidator(new LocalValidatorFactoryBean());
        return factory;
    }
    
    

    @Override
    public void configureKafkaListeners(KafkaListenerEndpointRegistrar registrar) {
        List<String> topics = List.of("action-items-topic-1", "action-items-topic-2", "action-items-topic-3");
        log.info("Configuring dynamic Kafka listeners for topics: {}", topics);
        
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

        endpoint.setConcurrency(1);
        endpoint.setAutoStartup(true);

        registrar.registerEndpoint(endpoint);
    }
}
