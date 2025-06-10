# GitLab CI/CD Pipeline for Sample Test Project

This document provides a comprehensive overview of the GitLab CI/CD pipeline implemented for the Sample Test Spring Boot application.

## Pipeline Overview

The pipeline is designed for a multi-module Maven Spring Boot project with the following characteristics:
- **Hexagonal Architecture**: Separated into domain, ports, and adapters
- **Avro Integration**: Uses Apache Avro for schema management
- **Spring Boot Application**: Main deployable artifact
- **PostgreSQL Database**: For data persistence
- **Kafka Integration**: With Avro serialization
- **Docker Containerization**: For deployment

## Pipeline Stages

### 1. **Validate** 🔍
- Validates the Maven project structure and dependencies
- Runs on: Merge requests, main branch, and tags

### 2. **Build** 🔨
- Compiles all modules in the multi-module Maven project
- Generates Avro Java classes from schemas
- Creates artifacts for subsequent stages
- Runs on: Merge requests, main branch, and tags

### 3. **Test** 🧪
- **Unit Tests**: Runs comprehensive unit tests across all modules
- **Integration Tests**: Runs integration tests with PostgreSQL service
- **Security Scan**: Performs container security scanning with Trivy
- Generates test reports and coverage metrics
- Runs on: Merge requests, main branch, and tags

### 4. **Package** 📦
- Creates JAR files for all modules
- Packages the Spring Boot application
- Creates the Avro JAR with generated classes
- Runs on: Main branch and tags

### 5. **Publish** 📤
- **Avro JAR Upload**: Uploads Avro JAR to Nexus on every main branch build
- **All Artifacts Upload**: Uploads all artifacts to Nexus on tagged releases
- Supports both snapshot and release repositories
- Runs on: Main branch (Avro only) and tags (all artifacts)

### 6. **Build Image** 🐳
- Creates optimized Docker image with OpenJDK 17
- Implements security best practices (non-root user)
- Includes health checks
- Pushes to GitLab Container Registry
- Tags with commit SHA, latest, and release tags
- Runs on: Main branch and tags

### 7. **Deploy** 🚀
- **Development**: 2 replicas, basic resources, manual trigger
- **Staging**: 3 replicas, enhanced resources, manual trigger
- **Production**: 5 replicas, high resources, rolling updates, manual trigger
- Uses Kubernetes manifests with proper resource allocation
- Implements health checks and readiness probes

## Avro JAR Upload Strategy

The pipeline uploads the Avro JAR to Nexus in two scenarios:

### 1. **Every Main Branch Build** (Snapshot)
- Uploads only the Avro module JAR
- Uses snapshot repository for development builds
- Ensures latest Avro schemas are always available

### 2. **Tagged Releases** (Release)
- Uploads all project artifacts including Avro JAR
- Uses release repository for stable versions
- Creates immutable release artifacts

## Key Features

### 🔒 **Security**
- Container security scanning with Trivy
- Non-root user in Docker containers
- Masked/protected sensitive variables
- Security-first Kubernetes deployments

### 📊 **Monitoring & Observability**
- JUnit test reports integration
- Code coverage tracking
- Health checks and readiness probes
- Spring Boot Actuator endpoints

### 🎯 **Performance Optimized**
- Maven dependency caching
- Parallel job execution where possible
- Efficient Docker layer caching
- Optimized resource allocation per environment

### 🔄 **Environment Strategy**
- **Development**: Quick feedback, lower resources
- **Staging**: Production-like testing environment
- **Production**: High availability, rolling updates

### 📈 **Scalability**
- Horizontal pod autoscaling ready
- Resource limits and requests defined
- Environment-specific scaling configurations

## Pipeline Configuration

### Maven Configuration
```xml
<distributionManagement>
    <repository>
        <id>nexus-releases</id>
        <name>Nexus Release Repository</name>
        <url>${env.NEXUS_BASE_URL}/repository/maven-releases/</url>
    </repository>
    <snapshotRepository>
        <id>nexus-snapshots</id>
        <name>Nexus Snapshot Repository</name>
        <url>${env.NEXUS_BASE_URL}/repository/maven-snapshots/</url>
    </snapshotRepository>
</distributionManagement>
```

### Docker Strategy
- **Base Image**: `openjdk:17-jdk-slim`
- **Security**: Non-root user execution
- **Health Checks**: Built-in health monitoring
- **Optimization**: Minimal layer count

### Kubernetes Deployment
- **Service Mesh Ready**: Labels and annotations for service discovery
- **Configuration Management**: Environment-specific secrets
- **Resource Management**: CPU and memory limits/requests
- **High Availability**: Multiple replicas with anti-affinity

## Usage Instructions

### 1. **Setup Variables**
Configure required variables in GitLab project settings (see `gitlab-variables.md`)

### 2. **Create Kubernetes Secrets**
```bash
# Development
kubectl create secret generic app-secrets -n sample-test-dev \
  --from-literal=database-url="jdbc:postgresql://postgres:5432/sample_db_dev" \
  --from-literal=database-username="postgres" \
  --from-literal=database-password="password"
```

### 3. **Trigger Pipeline**
- **Automatic**: Push to main branch or create merge request
- **Release**: Create and push a Git tag (e.g., `git tag v1.0.0 && git push origin v1.0.0`)

### 4. **Deploy to Environments**
- Navigate to GitLab CI/CD → Pipelines
- Find your pipeline and click on the deploy job for your target environment
- Click "Play" to trigger manual deployment

### 5. **Monitor Deployment**
- Check pipeline status in GitLab
- Monitor application health via actuator endpoints
- View logs in Kubernetes or container platform

## Customization

### Adding New Environments
1. Duplicate existing deploy job in `.gitlab-ci.yml`
2. Update environment-specific variables
3. Create corresponding Kubernetes secrets
4. Adjust resource allocation as needed

### Modifying Upload Strategy
To change when Avro JARs are uploaded:
1. Modify the `rules` section in `publish-avro-nexus` job
2. Adjust the Maven deploy command if needed
3. Update documentation accordingly

### Security Enhancements
- Add SAST/DAST scanning jobs
- Implement container signing
- Add compliance checking
- Enable dependency vulnerability scanning

## Troubleshooting

### Common Issues

1. **Nexus Upload Failures**
   - Verify NEXUS_* variables are set correctly
   - Check network connectivity to Nexus server
   - Validate repository permissions

2. **Docker Build Failures**
   - Ensure JAR files are created in package stage
   - Check Docker service availability
   - Verify registry authentication

3. **Kubernetes Deployment Issues**
   - Verify secrets are created in target namespace
   - Check RBAC permissions
   - Validate cluster connectivity

4. **Test Failures**
   - Review test reports in GitLab
   - Check database service connectivity
   - Verify test environment configuration

### Pipeline Optimization

- **Parallel Execution**: Jobs in the same stage run in parallel
- **Caching**: Maven dependencies are cached across builds
- **Artifacts**: Only necessary artifacts are preserved
- **Resource Usage**: Optimized container resource allocation

This pipeline provides a robust, scalable, and secure CI/CD solution for your Spring Boot application with comprehensive Avro integration and multi-environment deployment capabilities. 