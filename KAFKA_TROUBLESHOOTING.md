# Kafka Connection Troubleshooting Guide

## Problem: Application Continuously Tries to Connect to Kafka

When Kafka is unavailable, the application may continuously attempt to reconnect, consuming resources and making it difficult to stop gracefully.

## Solutions Implemented

### 1. Configuration Changes

**Connection Timeout & Retry Limits:**
- Added `retries: 3` to limit connection attempts
- Set `request.timeout.ms: 30000` for faster timeout
- Configured `admin.retries: 3` for AdminClient operations
- Set `auto-startup: false` for listeners to prevent automatic startup

**Consumer Configuration:**
- Added `CONNECTIONS_MAX_IDLE_MS_CONFIG: 540000` to manage idle connections
- Configured `REQUEST_TIMEOUT_MS_CONFIG: 30000` for faster failure detection

### 2. Health Monitoring

**KafkaHealthChecker Improvements:**
- Monitors Kafka availability every 60 seconds
- Limits consecutive connection attempts to 5
- Automatically stops consumers when Kafka is unavailable for extended periods
- Restarts consumers when Kafka becomes available again

### 3. Graceful Shutdown

**GracefulShutdownConfig:**
- Adds shutdown hooks for proper cleanup
- Ensures all consumers are stopped gracefully on application termination
- Provides logging for better visibility during shutdown

## How to Handle Kafka Unavailability

### Option 1: Stop the Application (Recommended)
```bash
# Find the Java process
ps aux | grep java | grep ActionItemApplication

# Kill gracefully (replace PID with actual process ID)
kill -TERM <PID>

# If graceful shutdown doesn't work, force kill
kill -9 <PID>
```

### Option 2: Use Application Actuator Endpoints
If actuator is enabled, you can shutdown via HTTP:
```bash
curl -X POST http://localhost:8080/actuator/shutdown
```

### Option 3: Use Ctrl+C in Terminal
If running from terminal, use `Ctrl+C` to send interrupt signal.

## Prevention Strategies

### 1. Start Kafka Before Application
```bash
# Start Kafka first
./kafka-server-start.sh ../config/server.properties

# Then start your application
mvn spring-boot:run
```

### 2. Use Docker Compose for Dependencies
Create a `docker-compose.yml` with dependency management:
```yaml
version: '3.8'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      
  kafka:
    image: confluentinc/cp-kafka:latest
    depends_on:
      - zookeeper
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      
  your-app:
    build: .
    depends_on:
      - kafka
    environment:
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
```

### 3. Profile-Based Configuration
Use Spring profiles for different environments:

**application-dev.yml** (with Kafka):
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    listener:
      auto-startup: true
```

**application-test.yml** (without Kafka):
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    listener:
      auto-startup: false
```

## Monitoring and Debugging

### Check Application Status
```bash
# Check if application is running
ps aux | grep java | grep ActionItem

# Check network connections to Kafka
lsof -i :9092

# Monitor application logs
tail -f logs/application.log
```

### Key Log Messages to Watch For
- "Kafka broker is now available" - Successful connection
- "Kafka broker unavailable" - Connection failure
- "Stopping further connection attempts" - Health checker stopped retries
- "Shutdown hook triggered" - Graceful shutdown initiated

## Configuration Reference

### Key Configuration Properties
```yaml
spring:
  kafka:
    properties:
      retries: 3                          # Limit connection retries
      request.timeout.ms: 30000           # Faster timeout
      admin.retries: 3                    # Admin client retry limit
    listener:
      auto-startup: false                 # Manual listener startup
  lifecycle:
    timeout-per-shutdown-phase: 30s       # Shutdown timeout
```

### Health Check Settings
- Check interval: 60 seconds
- Max consecutive failures: 5
- Connection timeout: 5 seconds
- Admin client retries: 1

## Emergency Commands

### Force Stop All Java Processes
```bash
# CAUTION: This will stop ALL Java processes
pkill -f java
```

### Kill Specific Process by Port
```bash
# Find process using port 9092 (if any)
lsof -ti:9092 | xargs kill -9
```

### Check System Resources
```bash
# Check memory usage
top -o MEM | head -20

# Check CPU usage
top -o CPU | head -20
```

## Best Practices

1. **Always start Kafka before the application**
2. **Use health checks to monitor Kafka availability**
3. **Implement circuit breaker patterns for external dependencies**
4. **Configure appropriate timeouts and retry limits**
5. **Use structured logging for better debugging**
6. **Test failure scenarios in development**
7. **Monitor application metrics in production**

## Support

For additional help:
1. Check application logs in `logs/` directory
2. Review Kafka broker logs
3. Monitor system resources (CPU, memory, network)
4. Use application health endpoints if available 