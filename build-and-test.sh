#!/bin/bash

# Build and Test Script for Sample Test Project
# This script mimics the CI/CD pipeline for local development

set -e  # Exit on any error

echo "🚀 Starting local build and test process..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_step() {
    echo -e "${BLUE}==== $1 ====${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Parse command line arguments
PUBLISH_AVRO_SNAPSHOT=false
PUBLISH_AVRO_RELEASE=false
AVRO_VERSION=""

while [[ $# -gt 0 ]]; do
    case $1 in
        --publish-avro-snapshot)
            PUBLISH_AVRO_SNAPSHOT=true
            shift
            ;;
        --publish-avro-release)
            PUBLISH_AVRO_RELEASE=true
            AVRO_VERSION="$2"
            shift 2
            ;;
        --help)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --publish-avro-snapshot     Publish Avro schemas as snapshot to Nexus"
            echo "  --publish-avro-release VER  Publish Avro schemas as release version to Nexus"
            echo "  --help                      Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0                                    # Basic build and test"
            echo "  $0 --publish-avro-snapshot           # Build and publish Avro snapshot"
            echo "  $0 --publish-avro-release 1.2.0      # Build and publish Avro release 1.2.0"
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    print_error "Maven is not installed. Please install Maven first."
    exit 1
fi

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    print_warning "Docker is not installed. Docker build will be skipped."
    SKIP_DOCKER=true
fi

# Validate Avro publishing requirements
if [ "$PUBLISH_AVRO_RELEASE" = true ]; then
    if [ -z "$AVRO_VERSION" ]; then
        print_error "Avro version must be specified for release publishing"
        exit 1
    fi
    if echo "$AVRO_VERSION" | grep -q "SNAPSHOT"; then
        print_error "Avro release version cannot contain SNAPSHOT"
        exit 1
    fi
    # Check if Nexus credentials are available
    if [ -z "$NEXUS_USERNAME" ] || [ -z "$NEXUS_PASSWORD" ] || [ -z "$NEXUS_BASE_URL" ]; then
        print_warning "Nexus credentials not set. Set NEXUS_USERNAME, NEXUS_PASSWORD, and NEXUS_BASE_URL environment variables"
        print_warning "Continuing with local build only..."
        PUBLISH_AVRO_RELEASE=false
    fi
fi

if [ "$PUBLISH_AVRO_SNAPSHOT" = true ]; then
    if [ -z "$NEXUS_USERNAME" ] || [ -z "$NEXUS_PASSWORD" ] || [ -z "$NEXUS_BASE_URL" ]; then
        print_warning "Nexus credentials not set. Set NEXUS_USERNAME, NEXUS_PASSWORD, and NEXUS_BASE_URL environment variables"
        print_warning "Continuing with local build only..."
        PUBLISH_AVRO_SNAPSHOT=false
    fi
fi

# Step 1: Validate
print_step "Validating Maven project"
mvn validate
print_success "Validation completed"

# Step 2: Clean and Compile
print_step "Cleaning and compiling project"
mvn clean compile
print_success "Compilation completed"

# Step 3: Run Tests
print_step "Running unit tests"
mvn test
print_success "Unit tests completed"

# Step 4: Package
print_step "Packaging application"
mvn package -DskipTests -Dmaven.deploy.skip=true
print_success "Packaging completed"

# Step 5: Check if JAR files are created
if [ -f "modules/application/target/application-1.0.0-SNAPSHOT.jar" ]; then
    print_success "Application JAR created successfully"
else
    print_error "Application JAR not found!"
    exit 1
fi

# Check for Avro artifacts
AVRO_JAR_PATTERN="modules/avro/target/avro-schemas-*.jar"
if ls $AVRO_JAR_PATTERN 1> /dev/null 2>&1; then
    AVRO_JAR=$(ls $AVRO_JAR_PATTERN | head -1)
    print_success "Avro JAR created successfully: $(basename $AVRO_JAR)"
else
    print_warning "Avro JAR not found (this might be normal if no Avro schemas exist)"
fi

# Step 6: Avro Publishing (if requested)
if [ "$PUBLISH_AVRO_SNAPSHOT" = true ] || [ "$PUBLISH_AVRO_RELEASE" = true ]; then
    print_step "Publishing Avro Schemas to Nexus"
    
    # Configure Maven settings for Nexus authentication
    mkdir -p ~/.m2
    cat > ~/.m2/settings.xml << EOF
<settings>
  <servers>
    <server>
      <id>nexus-releases</id>
      <username>${NEXUS_USERNAME}</username>
      <password>${NEXUS_PASSWORD}</password>
    </server>
    <server>
      <id>nexus-snapshots</id>
      <username>${NEXUS_USERNAME}</username>
      <password>${NEXUS_PASSWORD}</password>
    </server>
  </servers>
</settings>
EOF

    cd modules/avro
    
    if [ "$PUBLISH_AVRO_SNAPSHOT" = true ]; then
        print_step "Publishing Avro Snapshot"
        # Get current version and make it a snapshot
        CURRENT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
        if ! echo "$CURRENT_VERSION" | grep -q "SNAPSHOT"; then
            SNAPSHOT_VERSION="${CURRENT_VERSION}-SNAPSHOT"
            mvn versions:set -DnewVersion="$SNAPSHOT_VERSION"
        fi
        mvn clean deploy -Davro.publish.enabled=true
        print_success "Avro Snapshot published to Nexus"
    fi
    
    if [ "$PUBLISH_AVRO_RELEASE" = true ]; then
        print_step "Publishing Avro Release Version: $AVRO_VERSION"
        mvn versions:set -DnewVersion="$AVRO_VERSION"
        mvn clean deploy -Davro.publish.enabled=true
        print_success "Avro Release $AVRO_VERSION published to Nexus"
    fi
    
    cd ../..
fi

# Step 7: Docker Build (if Docker is available)
if [ "$SKIP_DOCKER" != "true" ]; then
    print_step "Building Docker image"
    
    # Use existing Dockerfile or create one
    if [ ! -f "Dockerfile" ]; then
        cat > Dockerfile << 'EOF'
FROM openjdk:17-jdk-slim

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Create non-root user
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

# Copy the JAR file
COPY modules/application/target/application-*.jar app.jar

# Change ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM optimization for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
EOF
        print_warning "Created temporary Dockerfile"
    fi
    
    docker build -t sample-test:latest .
    print_success "Docker image built successfully"
    
    # Show image details
    echo ""
    echo "Docker image details:"
    docker images sample-test:latest
else
    print_warning "Docker build skipped (Docker not available)"
fi

# Step 8: Display build summary
echo ""
print_step "Build Summary"
echo "📁 Project structure:"
find modules -name "*.jar" -type f | head -10

echo ""
echo "🎯 Available commands:"
echo "  Run application: java -jar modules/application/target/application-1.0.0-SNAPSHOT.jar"
if [ "$SKIP_DOCKER" != "true" ]; then
    echo "  Run with Docker: docker run -p 8080:8080 sample-test:latest"
fi
echo "  Run tests: mvn test"
echo "  Start database: docker-compose up postgres"

echo ""
echo "📦 Avro Schema Management:"
echo "  Publish snapshot: $0 --publish-avro-snapshot"
echo "  Publish release:  $0 --publish-avro-release 1.2.0"
echo "  Current Avro version: $(grep 'avro.schema.version' pom.xml | sed 's/.*>\(.*\)<.*/\1/' | tr -d ' ')"

if [ "$PUBLISH_AVRO_SNAPSHOT" = true ] || [ "$PUBLISH_AVRO_RELEASE" = true ]; then
    echo ""
    echo "🚀 Avro Publishing Summary:"
    if [ "$PUBLISH_AVRO_SNAPSHOT" = true ]; then
        echo "  ✅ Snapshot published to Nexus"
    fi
    if [ "$PUBLISH_AVRO_RELEASE" = true ]; then
        echo "  ✅ Release $AVRO_VERSION published to Nexus"
    fi
    echo "  📍 Check in Nexus: ${NEXUS_BASE_URL}/repository/maven-releases/com/example/avro-schemas/"
fi

echo ""
print_success "Local build completed successfully! 🎉"

# Optional: Run integration tests if database is available
if command -v docker-compose &> /dev/null; then
    echo ""
    read -p "Do you want to run integration tests with database? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        print_step "Starting database and running integration tests"
        docker-compose up -d postgres
        sleep 10  # Wait for database to be ready
        
        # Set test database environment variables
        export SPRING_PROFILES_ACTIVE=test
        export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/sample_db
        export SPRING_DATASOURCE_USERNAME=postgres
        export SPRING_DATASOURCE_PASSWORD=password
        
        mvn verify -Dspring.profiles.active=test
        print_success "Integration tests completed"
        
        # Stop database
        docker-compose down
    fi
fi 