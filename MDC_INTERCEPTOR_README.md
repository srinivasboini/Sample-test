# MDC Interceptor and Context Management

This document describes the MDC (Mapped Diagnostic Context) interceptor setup and configuration for distributed tracing and correlation in the Kafka-based application.

## Overview

The MDC interceptor provides automatic context management for Kafka messages, enabling distributed tracing, correlation, and enhanced logging throughout the application. It automatically sets up correlation IDs, Kafka metadata, and other contextual information for each message processed.

## Components

### 1. MdcKafkaInterceptor

**Location**: `modules/adapter-in/src/main/java/com/example/adapter/in/kafka/interceptor/MdcKafkaInterceptor.java`

A Kafka consumer interceptor that automatically sets up MDC context for each batch of messages consumed.

**Features**:
- Automatic correlation ID generation
- Kafka metadata tracking (topic, partition, offset, etc.)
- Thread-safe context management
- Context preservation across async operations

**Key Methods**:
- `onConsume()`: Sets up MDC context for incoming messages
- `setRecordContext()`: Updates context for specific records
- `getCorrelationId()`: Retrieves current correlation ID
- `clearRecordContext()`: Clears record-specific context

### 2. MdcKafkaConfig

**Location**: `modules/adapter-in/src/main/java/com/example/adapter/in/kafka/config/MdcKafkaConfig.java`

Configuration class that sets up consumer factories with MDC interceptor support.

**Features**:
- MDC-enabled consumer factory creation
- Avro-specific consumer factory
- Interceptor configuration
- Property management

### 3. MdcUtils

**Location**: `modules/commons/src/main/java/com/example/commons/mdc/MdcUtils.java`

Utility class providing convenient MDC operations throughout the application.

**Features**:
- Correlation ID management
- Context preservation utilities
- Kafka-specific context helpers
- Thread-safe operations

### 4. AsyncConfig

**Location**: `modules/commons/src/main/java/com/example/commons/async/AsyncConfig.java`

Configuration for async operations with MDC context preservation.

**Features**:
- Task decorator for MDC context preservation
- Context propagation across async boundaries
- Multiple executor configurations

## Configuration

### 1. Application Properties

Add the following properties to your `application.yml`:

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: action-item-consumer-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: io.confluent.kafka.serializers.KafkaAvroDeserializer
    properties:
      schema.registry.url: http://localhost:8081
      specific.avro.reader: true
      auto.register.schemas: false
    listener:
      observation-enabled: true

logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} [correlationId=%X{correlationId}, topic=%X{kafka.topic}, partition=%X{kafka.partition}, offset=%X{kafka.offset}] - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} [correlationId=%X{correlationId}, topic=%X{kafka.topic}, partition=%X{kafka.partition}, offset=%X{kafka.offset}] - %msg%n"
  level:
    com.example.adapter.in.kafka.interceptor: DEBUG
    com.example.commons.mdc: DEBUG
```

### 2. Logback Configuration

Create or update `logback-spring.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} [correlationId=%X{correlationId}, topic=%X{kafka.topic}, partition=%X{kafka.partition}, offset=%X{kafka.offset}] - %msg%n</pattern>
        </encoder>
    </appender>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/application.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} [correlationId=%X{correlationId}, topic=%X{kafka.topic}, partition=%X{kafka.partition}, offset=%X{kafka.offset}] - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE" />
        <appender-ref ref="FILE" />
    </root>
</configuration>
```

## Usage Patterns

### 1. Basic MDC Operations

```java
import com.example.commons.mdc.MdcUtils;

// Set correlation ID
String correlationId = MdcUtils.setCorrelationId();

// Set additional context
MdcUtils.setComponent("MyComponent");
MdcUtils.setOperation("processMessage");

// Get current correlation ID
String currentCorrelationId = MdcUtils.getCorrelationId();

// Clear context
MdcUtils.clear();
```

### 2. Kafka Context Management

```java
import com.example.commons.mdc.MdcUtils;

// Set Kafka-specific context
MdcUtils.setKafkaContext("my-topic", 0, 123L, "message-key", System.currentTimeMillis(), "my-group");

// Clear Kafka context
MdcUtils.clearKafkaContext();
```

### 3. Context Preservation

```java
import com.example.commons.mdc.MdcUtils;

// Execute with preserved context
MdcUtils.withContext(() -> {
    // Your code here
    log.info("Processing with preserved context");
});

// Execute supplier with preserved context
String result = MdcUtils.withContext(() -> {
    // Your code here
    return "result";
});
```

### 4. Async Operations

The MDC context is automatically preserved across async operations when using the configured executors:

```java
@Autowired
@Qualifier("messageProcessingExecutor")
private Executor messageProcessingExecutor;

CompletableFuture.runAsync(() -> {
    // MDC context is automatically preserved here
    log.info("Processing in async thread with correlation ID: {}", MdcUtils.getCorrelationId());
}, messageProcessingExecutor);
```

## MDC Keys

### Standard Keys

- `correlationId`: Unique identifier for request correlation
- `requestId`: Request-specific identifier
- `userId`: User identifier
- `sessionId`: Session identifier
- `threadId`: Thread identifier
- `component`: Component identifier
- `operation`: Operation identifier

### Kafka-Specific Keys

- `kafka.topic`: Kafka topic name
- `kafka.partition`: Partition number
- `kafka.offset`: Message offset
- `kafka.messageKey`: Message key
- `kafka.timestamp`: Message timestamp
- `kafka.consumerGroup`: Consumer group name

## Logging Examples

With the configured logging pattern, you'll see output like:

```
2024-01-15 10:30:45 [kafka-msg-processor-1] INFO  c.e.a.i.k.ActionItemKafkaConsumer [correlationId=550e8400-e29b-41d4-a716-446655440000, topic=action-items-topic-1, partition=0, offset=123] - Received message with key: user123
2024-01-15 10:30:45 [kafka-msg-processor-1] INFO  c.e.a.i.k.h.ActionItemAsyncMessageHandler [correlationId=550e8400-e29b-41d4-a716-446655440000, topic=action-items-topic-1, partition=0, offset=123] - Handling async request
2024-01-15 10:30:46 [kafka-msg-processor-1] INFO  c.e.a.i.k.h.ActionItemMessageProcessor [correlationId=550e8400-e29b-41d4-a716-446655440000, topic=action-items-topic-1, partition=0, offset=123] - Processing action item message
```

## Monitoring and Observability

### 1. Correlation Tracking

The correlation ID allows you to track a message through the entire processing pipeline:

```java
// In any component
String correlationId = MdcUtils.getCorrelationId();
log.info("Processing message with correlation ID: {}", correlationId);
```

### 2. Distributed Tracing

When combined with Spring Cloud Sleuth or Micrometer Tracing, the correlation ID can be used for distributed tracing:

```java
// The correlation ID is automatically included in trace spans
@Observed(name = "process.action.item", contextualName = "action-item-processor")
public void processActionItem(ActionItemAsyncRequest request) {
    // Processing logic
}
```

### 3. Metrics and Monitoring

You can create custom metrics based on MDC context:

```java
@Component
public class MdcMetrics {
    
    private final MeterRegistry meterRegistry;
    
    public void recordProcessingTime(String topic, long duration) {
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(Timer.builder("kafka.processing.time")
            .tag("topic", topic)
            .tag("correlationId", MdcUtils.getCorrelationId())
            .register(meterRegistry));
    }
}
```

## Best Practices

### 1. Context Management

- Always clear context when done processing
- Use try-finally blocks for context cleanup
- Preserve context across async boundaries

### 2. Performance Considerations

- MDC operations are thread-local and fast
- Avoid storing large objects in MDC
- Use correlation IDs for tracking, not full objects

### 3. Logging Patterns

- Include correlation ID in all log messages
- Use structured logging for better parsing
- Include relevant context in error messages

### 4. Error Handling

```java
try {
    // Processing logic
} catch (Exception e) {
    log.error("Error processing message with correlationId: {}", 
              MdcUtils.getCorrelationId(), e);
    throw e;
} finally {
    // Clean up context if needed
    MdcUtils.clearRecordContext();
}
```

## Troubleshooting

### 1. Missing Correlation ID

If correlation IDs are missing in logs:

- Check that the interceptor is properly configured
- Verify the logging pattern includes MDC keys
- Ensure the consumer factory is using the MDC interceptor

### 2. Context Not Preserved in Async Operations

If context is lost in async operations:

- Verify the TaskDecorator is configured
- Check that you're using the configured executors
- Ensure the async configuration is loaded

### 3. Performance Issues

If you experience performance issues:

- Monitor MDC operation frequency
- Check for memory leaks in context maps
- Verify context cleanup is happening properly

## Testing

### 1. Unit Testing

```java
@Test
void testMdcContextPreservation() {
    // Set up context
    MdcUtils.setCorrelationId("test-correlation-id");
    MdcUtils.setComponent("test-component");
    
    // Verify context is set
    assertEquals("test-correlation-id", MdcUtils.getCorrelationId());
    assertEquals("test-component", MdcUtils.getComponent());
    
    // Clean up
    MdcUtils.clear();
}
```

### 2. Integration Testing

```java
@SpringBootTest
class MdcIntegrationTest {
    
    @Test
    void testKafkaMessageWithMdc() {
        // Send test message
        // Verify MDC context is set in consumer
        // Check logs for correlation ID
    }
}
```

## Summary

The MDC interceptor provides comprehensive context management for Kafka-based applications, enabling:

- **Distributed Tracing**: Track messages across services
- **Correlation**: Link related operations and logs
- **Enhanced Logging**: Include context in all log messages
- **Async Support**: Preserve context across async boundaries
- **Monitoring**: Create metrics and alerts based on context

This setup ensures that every Kafka message can be traced through the entire processing pipeline, making debugging and monitoring much more effective. 