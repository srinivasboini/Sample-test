# Database Health Monitor with Kafka Consumer Control

## Overview

This system automatically detects database downtime and pauses Kafka consumers to prevent message processing failures. When the database recovers, consumers are automatically resumed.

## Architecture

### Components

1. **DatabaseConfig** - Existing database health check mechanism
2. **DatabaseHealthMonitor** - New component that controls Kafka consumers
3. **DatabaseHealthController** - REST API for monitoring and manual control
4. **KafkaListenerEndpointRegistry** - Spring Kafka component for consumer management

### Flow Diagram

```
┌─────────────────┐    ┌─────────────────────┐    ┌─────────────────┐
│   Database      │    │  DatabaseHealth     │    │   Kafka         │
│   Health Check  │───▶│  Monitor            │───▶│   Consumers     │
│   (15s interval)│    │  (5s interval)      │    │                 │
└─────────────────┘    └─────────────────────┘    └─────────────────┘
                                │
                                ▼
                       ┌─────────────────────┐
                       │  REST API           │
                       │  (Monitoring)       │
                       └─────────────────────┘
```

## Configuration

### Application Properties

```yaml
# Database Health Monitor Configuration
database:
  health:
    monitor:
      enabled: true
      check-interval: 5000        # 5 seconds
      downtime-threshold: 10000   # 10 seconds
      recovery-threshold: 5000    # 5 seconds
      max-consecutive-failures: 3
```

### Thresholds Explained

- **Check Interval**: How often the health monitor runs (5 seconds)
- **Downtime Threshold**: How long to wait before pausing consumers (10 seconds)
- **Recovery Threshold**: How long to wait before resuming consumers (5 seconds)
- **Max Consecutive Failures**: Number of failures before considering database down (3)

## How It Works

### 1. Database Health Monitoring

The system uses the existing `DatabaseConfig.healthCheck()` method to monitor database connectivity:

```java
@Scheduled(fixedRate = 15000) // Every 15 seconds
public void healthCheck() {
    // Performs SELECT 1 query
    // Attempts recovery if needed
}
```

### 2. Consumer Control Logic

The `DatabaseHealthMonitor` runs every 5 seconds and:

1. **Calls the database health check**
2. **Tracks consecutive failures**
3. **Pauses consumers when database is down**
4. **Resumes consumers when database recovers**

### 3. Circuit Breaker Pattern

The system implements a circuit breaker pattern:

- **Closed State**: Database healthy, consumers running
- **Open State**: Database down, consumers paused
- **Half-Open State**: Database recovered, waiting to resume consumers

## REST API Endpoints

### Health Status Endpoints

```bash
# Get database health status
GET /api/health/database

# Get consumer status
GET /api/health/consumers

# Get comprehensive health status
GET /api/health/status

# Get detailed metrics
GET /api/health/metrics

# Manual health check trigger
POST /api/health/check

# Simple ping endpoint
GET /api/health/ping
```

### Example Responses

#### Database Health
```json
{
  "healthy": true,
  "timeSinceLastSuccess": 5000,
  "consecutiveFailures": 0,
  "timestamp": 1703123456789
}
```

#### Consumer Status
```json
{
  "paused": false,
  "timestamp": 1703123456789
}
```

#### Comprehensive Status
```json
{
  "database": {
    "healthy": true,
    "timeSinceLastSuccess": 5000,
    "consecutiveFailures": 0
  },
  "consumers": {
    "paused": false
  },
  "system": {
    "timestamp": 1703123456789,
    "uptime": 3600000
  }
}
```

## Monitoring and Logging

### Key Log Messages

#### Database Recovery
```
✅ Database recovered! Last failure was 15000ms ago
▶️ RESUMING all Kafka consumers after database recovery
▶️ All 3 Kafka consumers have been RESUMED
```

#### Database Downtime
```
❌ Database health check failed: Connection refused
🛑 PAUSING all Kafka consumers due to database downtime
⏸️ Paused consumer: group-action-items-topic-1 (topic: [action-items-topic-1])
🛑 All 3 Kafka consumers have been PAUSED
```

#### Health Monitor Status
```
📊 Database Health Monitor Status - Healthy: true, Consumers Paused: false, Time Since Success: 5000ms, Consecutive Failures: 0
```

### Log Levels

Configure logging levels in `application.yml`:

```yaml
logging:
  level:
    com.example.application.config.DatabaseHealthMonitor: INFO
    com.example.application.controller.DatabaseHealthController: INFO
    com.example.application.config.DatabaseConfig: INFO
```

## Testing the System

### 1. Start the Application

```bash
mvn spring-boot:run
```

### 2. Monitor Health Status

```bash
# Check database health
curl http://localhost:18080/api/health/database

# Check consumer status
curl http://localhost:18080/api/health/consumers

# Get comprehensive status
curl http://localhost:18080/api/health/status
```

### 3. Simulate Database Downtime

```bash
# Stop PostgreSQL
sudo systemctl stop postgresql

# Or if using Docker
docker stop postgres-container
```

### 4. Observe Consumer Pause

Watch the logs for:
```
❌ Database health check failed: Connection refused
🛑 PAUSING all Kafka consumers due to database downtime
```

### 5. Restore Database

```bash
# Start PostgreSQL
sudo systemctl start postgresql

# Or if using Docker
docker start postgres-container
```

### 6. Observe Consumer Resume

Watch the logs for:
```
✅ Database recovered! Last failure was 15000ms ago
▶️ RESUMING all Kafka consumers after database recovery
```

## Configuration Options

### Customizing Thresholds

You can customize the behavior by modifying the constants in `DatabaseHealthMonitor`:

```java
// Configuration for downtime detection
private static final long DOWNTIME_THRESHOLD_MS = 10000; // 10 seconds
private static final long RECOVERY_THRESHOLD_MS = 5000;  // 5 seconds
private static final int MAX_CONSECUTIVE_FAILURES = 3;
```

### Environment-Specific Settings

#### Development
```yaml
database:
  health:
    monitor:
      check-interval: 3000      # Faster checks
      downtime-threshold: 5000  # Quick pause
      recovery-threshold: 2000  # Quick resume
```

#### Production
```yaml
database:
  health:
    monitor:
      check-interval: 10000     # Slower checks
      downtime-threshold: 30000 # Longer pause threshold
      recovery-threshold: 10000 # Longer recovery threshold
```

## Troubleshooting

### Common Issues

#### 1. Consumers Not Pausing

**Symptoms**: Database is down but consumers continue processing
**Causes**: 
- Health check not running
- Thresholds too high
- Kafka listener registry not found

**Solutions**:
```bash
# Check if health monitor is running
curl http://localhost:18080/api/health/status

# Check logs for health monitor messages
tail -f logs/application.log | grep DatabaseHealthMonitor
```

#### 2. Consumers Not Resuming

**Symptoms**: Database is up but consumers remain paused
**Causes**:
- Recovery threshold too high
- Health check failing
- Manual pause state

**Solutions**:
```bash
# Check database health
curl http://localhost:18080/api/health/database

# Manually trigger health check
curl -X POST http://localhost:18080/api/health/check
```

#### 3. High CPU Usage

**Symptoms**: Application using excessive CPU
**Causes**:
- Health check interval too frequent
- Database queries too heavy

**Solutions**:
```yaml
# Increase check interval
database:
  health:
    monitor:
      check-interval: 10000  # 10 seconds instead of 5
```

### Debug Mode

Enable debug logging for detailed troubleshooting:

```yaml
logging:
  level:
    com.example.application.config.DatabaseHealthMonitor: DEBUG
    com.example.application.config.DatabaseConfig: DEBUG
```

## Integration with Monitoring Systems

### Prometheus Metrics

The system exposes metrics via Spring Boot Actuator:

```bash
# Get Prometheus metrics
curl http://localhost:18080/actuator/prometheus
```

### Grafana Dashboard

Create a dashboard with these key metrics:

1. **Database Health**: `database_healthy`
2. **Consumer Status**: `consumers_paused`
3. **Response Time**: `database_time_since_last_success`
4. **Failure Count**: `database_consecutive_failures`

### Alerting Rules

Example Prometheus alerting rules:

```yaml
groups:
  - name: database-health
    rules:
      - alert: DatabaseDown
        expr: database_healthy == 0
        for: 30s
        labels:
          severity: critical
        annotations:
          summary: "Database is down"
          description: "Database has been down for more than 30 seconds"

      - alert: ConsumersPaused
        expr: consumers_paused == 1
        for: 60s
        labels:
          severity: warning
        annotations:
          summary: "Kafka consumers are paused"
          description: "Consumers have been paused due to database issues"
```

## Best Practices

### 1. Monitoring

- Set up alerts for database downtime
- Monitor consumer lag during pause/resume cycles
- Track recovery times

### 2. Configuration

- Adjust thresholds based on your database's typical recovery time
- Use different settings for development and production
- Test the system regularly

### 3. Logging

- Use structured logging for better analysis
- Set appropriate log levels for different environments
- Monitor log volume to prevent disk space issues

### 4. Testing

- Regularly test database failure scenarios
- Validate consumer pause/resume behavior
- Test with different database recovery times

## Security Considerations

### API Security

The health endpoints are currently unprotected. In production, consider:

1. **Authentication**: Add basic auth or JWT tokens
2. **Authorization**: Restrict access to monitoring teams
3. **Rate Limiting**: Prevent abuse of manual health check endpoint

### Network Security

1. **Database Access**: Ensure database connections are secure
2. **Kafka Access**: Secure Kafka cluster access
3. **API Access**: Use HTTPS for health endpoints

## Performance Impact

### Resource Usage

- **CPU**: Minimal impact (health check every 5-15 seconds)
- **Memory**: Low memory footprint
- **Network**: Small database queries for health checks

### Optimization Tips

1. **Health Check Query**: Use lightweight queries (SELECT 1)
2. **Connection Pooling**: Leverage existing HikariCP configuration
3. **Async Processing**: Health checks are non-blocking

## Future Enhancements

### Planned Features

1. **Configurable Thresholds**: Make thresholds configurable via properties
2. **Health Check Customization**: Allow custom health check queries
3. **Consumer Group Management**: Selective pause/resume of consumer groups
4. **Metrics Export**: Export health metrics to external monitoring systems
5. **Webhook Notifications**: Send notifications on state changes

### Integration Ideas

1. **Kubernetes Health Checks**: Integrate with K8s liveness/readiness probes
2. **Service Mesh**: Integrate with Istio/Linkerd health checks
3. **Cloud Native**: Add support for cloud database services
4. **Multi-Database**: Support for multiple database types 