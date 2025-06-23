# Kafka Observation Configuration Guide

## Overview

Spring Kafka 3.0+ provides built-in support for Micrometer observation, which enables comprehensive monitoring and tracing of Kafka operations. This guide explains how to configure observation settings from your `application.yml` file.

## Configuration Options

### 1. Basic Observation Enablement

Add the following to your `application.yml`:

```yaml
spring:
  kafka:
    listener:
      # Enable Micrometer observation for Kafka containers
      observation-enabled: true
```

### 2. Complete Configuration Example

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    properties:
      schema.registry.url: http://localhost:8081
    
    consumer:
      group-id: action-items-group
      auto-offset-reset: earliest
      enable-auto-commit: false
      properties:
        session.timeout.ms: 30000
        heartbeat.interval.ms: 10000
        max.poll.interval.ms: 300000
        max.poll.records: 100
        specific.avro.reader: true
    
    listener:
      ack-mode: MANUAL
      type: SINGLE
      concurrency: 3
      missing-topics-fatal: false
      auto-startup: false
      # Enable observation for detailed monitoring
      observation-enabled: true

# Management and monitoring configuration
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
  tracing:
    sampling:
      probability: 1.0
  observations:
    key-values:
      application: action-items-service
```

## Java Configuration

### KafkaConfig.java

Update your Kafka configuration class to read the observation setting:

```java
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.properties.schema.registry.url}")
    private String schemaRegistryUrl;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.listener.observation-enabled:false}")
    private boolean observationEnabled;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ActionItemAvro> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ActionItemAvro> factory =
            new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());

        // Configure Manual Acknowledgment
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        // Configure Observation
        factory.getContainerProperties().setObservationEnabled(observationEnabled);

        // Configure Concurrency
        factory.setConcurrency(3);

        // Configure Error Handler
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
            new FixedBackOff(5000L, 3L)
        );
        factory.setCommonErrorHandler(errorHandler);
       
        return factory;
    }
}
```

## What Observation Provides

When `observation-enabled: true` is set, Spring Kafka automatically creates observations for:

### 1. Consumer Operations
- **Message Consumption**: Tracks individual message processing
- **Batch Processing**: Monitors batch-level operations
- **Offset Management**: Tracks commit operations
- **Error Handling**: Captures processing failures

### 2. Metrics Generated
- `kafka.consumer.records.lag` - Consumer lag
- `kafka.consumer.records.consumed` - Messages consumed
- `kafka.consumer.records.consumed.rate` - Consumption rate
- `kafka.consumer.records.consumed.total` - Total messages consumed
- `kafka.consumer.records.consumed.rate` - Consumption rate
- `kafka.consumer.records.consumed.rate` - Consumption rate

### 3. Tracing Information
- **Trace IDs**: Distributed tracing across services
- **Span Context**: Message flow tracking
- **Correlation IDs**: Request correlation
- **Timing Data**: Performance metrics

## Custom Observation with @Observed

You can also add custom observations to your consumer methods:

```java
@Component
@RequiredArgsConstructor
public class ActionItemKafkaConsumer {

    @Observed(name = "kafka.consumer.action.item", 
              contextualName = "kafka-consumer", 
              lowCardinalityKeyValues = {"consumer.type", "action-item"})
    public void consume(ConsumerRecord<String, ActionItemAvro> record, Acknowledgment acknowledgment) {
        // Your processing logic
    }
}
```

## Environment-Specific Configuration

### Development Environment
```yaml
spring:
  kafka:
    listener:
      observation-enabled: true
  profiles:
    active: dev

logging:
  level:
    org.springframework.kafka: DEBUG
    io.micrometer: DEBUG
```

### Production Environment
```yaml
spring:
  kafka:
    listener:
      observation-enabled: true
  profiles:
    active: prod

management:
  metrics:
    export:
      prometheus:
        enabled: true
  tracing:
    sampling:
      probability: 0.1  # Sample 10% of traces in production
```

### Testing Environment
```yaml
spring:
  kafka:
    listener:
      observation-enabled: false  # Disable for testing
  profiles:
    active: test
```

## Monitoring and Visualization

### 1. Prometheus Metrics
Access metrics at: `http://localhost:18080/actuator/prometheus`

Key metrics to monitor:
- `kafka_consumer_records_lag`
- `kafka_consumer_records_consumed_total`
- `kafka_consumer_records_consumed_rate`

### 2. Grafana Dashboards
Create dashboards to visualize:
- Consumer lag trends
- Message processing rates
- Error rates
- Processing latency

### 3. Distributed Tracing
Use tools like:
- **Jaeger**: For trace visualization
- **Zipkin**: For distributed tracing
- **Spring Cloud Sleuth**: For trace correlation

## Performance Considerations

### 1. Sampling
In high-throughput environments, consider sampling:

```yaml
management:
  tracing:
    sampling:
      probability: 0.1  # Sample 10% of traces
```

### 2. Metrics Cardinality
Use low-cardinality key values to prevent metric explosion:

```java
@Observed(name = "kafka.consumer", 
          lowCardinalityKeyValues = {"consumer.type", "action-item"})
```

### 3. Memory Usage
Monitor memory usage when observation is enabled, especially in high-throughput scenarios.

## Troubleshooting

### Common Issues

1. **Observation Not Working**
   - Ensure `spring-kafka` version 3.0+
   - Check that `observation-enabled: true` is set
   - Verify Micrometer dependencies are included

2. **High Memory Usage**
   - Reduce sampling probability
   - Use low-cardinality key values
   - Monitor and adjust buffer sizes

3. **Missing Metrics**
   - Check actuator endpoints are exposed
   - Verify Prometheus export is enabled
   - Ensure proper metric naming

### Debug Configuration

Add debug logging to troubleshoot observation setup:

```yaml
logging:
  level:
    org.springframework.kafka: DEBUG
    io.micrometer: DEBUG
    org.springframework.kafka.listener: DEBUG
```

## Best Practices

1. **Enable in Production**: Always enable observation in production for monitoring
2. **Use Sampling**: Implement sampling for high-throughput scenarios
3. **Monitor Performance**: Watch for performance impact of observation
4. **Custom Metrics**: Add custom observations for business-specific metrics
5. **Alerting**: Set up alerts based on observation metrics
6. **Documentation**: Document custom observation patterns

## Dependencies

Ensure you have the required dependencies:

```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

## Summary

By setting `spring.kafka.listener.observation-enabled: true` in your `application.yml`, you enable comprehensive monitoring and tracing of Kafka operations. This provides valuable insights into:

- Message processing performance
- Consumer lag and throughput
- Error rates and patterns
- Distributed tracing across services
- Operational health metrics

The observation feature integrates seamlessly with Spring Boot Actuator and Micrometer, providing a complete monitoring solution for your Kafka-based applications. 