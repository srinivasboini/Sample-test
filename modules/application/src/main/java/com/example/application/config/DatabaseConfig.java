package com.example.application.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;

@Slf4j
@Configuration
public class DatabaseConfig {

    @Value("${spring.datasource.url:jdbc:postgresql://localhost:5432/sample_db}")
    private String databaseUrl;

    @Value("${spring.datasource.username:postgres}")
    private String username;

    @Value("${spring.datasource.password:password}")
    private String password;

    @Value("${spring.datasource.hikari.maximum-pool-size:20}")
    private int maximumPoolSize;

    @Value("${spring.datasource.hikari.minimum-idle:5}")
    private int minimumIdle;

    @Value("${spring.datasource.hikari.connection-timeout:30000}")
    private long connectionTimeout;

    @Value("${spring.datasource.hikari.idle-timeout:600000}")
    private long idleTimeout;

    @Value("${spring.datasource.hikari.max-lifetime:600000}")
    private long maxLifetime;

    @Value("${spring.datasource.hikari.leak-detection-threshold:60000}")
    private long leakDetectionThreshold;

    private HikariDataSource dataSource;

    @Bean
    @Primary
    @ConditionalOnMissingBean(DataSource.class)
    public DataSource dataSource() {
        this.dataSource = createDataSource();
        return this.dataSource;
    }

    private HikariDataSource createDataSource() {
        HikariConfig config = new HikariConfig();
        
        // Database connection properties
        config.setJdbcUrl(databaseUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");

        // Pool configuration for resilience
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(minimumIdle);
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        config.setLeakDetectionThreshold(leakDetectionThreshold);

        // Connection validation and recovery
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(Duration.ofSeconds(3).toMillis());
        
        // Enterprise auto-recovery properties
        config.setInitializationFailTimeout(-1); // Never fail on startup - keep retrying
        config.setRegisterMbeans(true);
        config.setPoolName("PostgreSQL-HikariPool");
        config.setKeepaliveTime(Duration.ofMinutes(10).toMillis()); // More frequent keepalive to detect issues faster
        config.setScheduledExecutor(null); // Use default scheduler for connection management

        // PostgreSQL-specific optimizations
        config.setAutoCommit(false); // Ensure autoCommit is disabled for transaction management
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("ApplicationName", "ActionItemApp");

        log.info("Initializing HikariCP connection pool with URL: {}", databaseUrl);
        return new HikariDataSource(config);
    }

    private volatile boolean isRecovering = false;
    private volatile long lastSuccessfulCheck = System.currentTimeMillis();

    @Scheduled(fixedRate = 15000) // Check every 15 seconds for faster recovery
    public void healthCheck() {
        log.info("Health check started");
        
        if (isRecovering) {
            log.debug("Recovery in progress, skipping health check");
            return;
        }

        if (dataSource == null || dataSource.isClosed()) {
            log.warn("DataSource is null or closed, attempting recovery...");
            attemptRecovery();
            return;
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement("SELECT 1")) {
            
            stmt.executeQuery();
            lastSuccessfulCheck = System.currentTimeMillis();
            
            // Only log success every 2 minutes to reduce noise
            if (System.currentTimeMillis() % 120000 < 15000) {
                log.info("Database health check PASSED - Pool stats: Active={}, Idle={}, Total={}", 
                         dataSource.getHikariPoolMXBean().getActiveConnections(),
                         dataSource.getHikariPoolMXBean().getIdleConnections(),
                         dataSource.getHikariPoolMXBean().getTotalConnections());
            }
                     
        } catch (Exception e) {
            long timeSinceLastSuccess = System.currentTimeMillis() - lastSuccessfulCheck;
            log.error("Database health check FAILED after {}ms: {} - Attempting recovery...", 
                     timeSinceLastSuccess, e.getMessage());
            attemptRecovery();
        }
    }

    private void attemptRecovery() {
        if (isRecovering) {
            log.debug("Recovery already in progress, skipping duplicate attempt");
            return;
        }
        
        isRecovering = true;
        try {
            log.info("🔄 Starting automatic database connection recovery...");
            
            if (dataSource != null && !dataSource.isClosed()) {
                // Step 1: Evict all stale connections
                log.info("Step 1: Purging stale connections from pool");
                dataSource.getHikariPoolMXBean().softEvictConnections();
                Thread.sleep(1000);
                
                // Step 2: Aggressive connection cleanup
                try {
                    dataSource.getHikariPoolMXBean().suspendPool();
                    Thread.sleep(2000);
                    dataSource.getHikariPoolMXBean().resumePool();
                    log.info("Step 2: Pool suspend/resume cycle completed");
                } catch (Exception e) {
                    log.warn("Pool management warning: {}", e.getMessage());
                }
                
                // Step 3: Test connectivity with multiple attempts
                for (int attempt = 1; attempt <= 3; attempt++) {
                    try (Connection testConnection = dataSource.getConnection();
                         PreparedStatement stmt = testConnection.prepareStatement("SELECT 1")) {
                        
                        stmt.executeQuery();
                        log.info("✅ Database connection recovery SUCCESSFUL on attempt {}", attempt);
                        lastSuccessfulCheck = System.currentTimeMillis();
                        return;
                        
                    } catch (SQLException sqlEx) {
                        if (attempt < 3) {
                            log.warn("❌ Recovery attempt {} failed: {} - Retrying in 2s...", 
                                   attempt, sqlEx.getMessage());
                            Thread.sleep(2000);
                        } else {
                            log.error("❌ All recovery attempts failed. Database may be down: {}", 
                                    sqlEx.getMessage());
                        }
                    }
                }
                
            } else {
                log.error("DataSource is null or closed - cannot perform recovery");
            }
            
        } catch (Exception e) {
            log.error("Recovery process encountered error: {}", e.getMessage());
        } finally {
            isRecovering = false;
        }
    }

    public void logPoolStats() {
        if (dataSource != null && !dataSource.isClosed()) {
            var poolMXBean = dataSource.getHikariPoolMXBean();
            log.info("HikariCP Pool Stats - Active: {}, Idle: {}, Total: {}, Threads Waiting: {}",
                    poolMXBean.getActiveConnections(),
                    poolMXBean.getIdleConnections(),
                    poolMXBean.getTotalConnections(),
                    poolMXBean.getThreadsAwaitingConnection());
        }
    }

} 