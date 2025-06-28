#!/bin/bash

# Deployment Script for Sample Test Project to Nexus
# Usage: ./deploy-to-nexus.sh [--version <version>] [--avro-only] [--app-only]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

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
DEPLOY_VERSION=""
AVRO_ONLY=false
APP_ONLY=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --version)
            DEPLOY_VERSION="$2"
            shift 2
            ;;
        --avro-only)
            AVRO_ONLY=true
            shift
            ;;
        --app-only)
            APP_ONLY=true
            shift
            ;;
        --help)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Options:"
            echo "  --version <version>    Specify version to deploy (e.g., 1.0.0 or 1.0.0-SNAPSHOT)"
            echo "  --avro-only           Deploy only Avro schemas"
            echo "  --app-only            Deploy only application (skip Avro)"
            echo "  --help                Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0                                    # Deploy current version"
            echo "  $0 --version 1.0.0                   # Deploy release version 1.0.0"
            echo "  $0 --version 1.0.0-SNAPSHOT          # Deploy snapshot version"
            echo "  $0 --avro-only                       # Deploy only Avro schemas"
            echo "  $0 --app-only --version 1.0.0        # Deploy only app with specific version"
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

# Check Nexus credentials
if [ -z "$NEXUS_USERNAME" ] || [ -z "$NEXUS_PASSWORD" ] || [ -z "$NEXUS_BASE_URL" ]; then
    print_error "Nexus credentials not set. Please set the following environment variables:"
    print_error "  NEXUS_USERNAME"
    print_error "  NEXUS_PASSWORD" 
    print_error "  NEXUS_BASE_URL"
    exit 1
fi

# Configure Maven settings for Nexus authentication
print_step "Configuring Maven settings for Nexus authentication"
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
print_success "Maven settings configured"

# Update version if specified
if [ -n "$DEPLOY_VERSION" ]; then
    print_step "Updating project version to: $DEPLOY_VERSION"
    ./update-version.sh "$DEPLOY_VERSION"
    print_success "Version updated"
fi

# Get current version
CURRENT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
print_step "Current project version: $CURRENT_VERSION"

# Determine if this is a snapshot or release
IS_SNAPSHOT=false
if echo "$CURRENT_VERSION" | grep -q "SNAPSHOT"; then
    IS_SNAPSHOT=true
    print_warning "Deploying SNAPSHOT version to snapshot repository"
else
    print_warning "Deploying RELEASE version to release repository"
fi

# Step 1: Clean and compile
print_step "Cleaning and compiling project"
mvn clean compile -DskipTests
print_success "Compilation completed"

# Step 2: Run tests
print_step "Running tests"
mvn test
print_success "Tests completed"

# Step 3: Package
print_step "Packaging application"
mvn package -DskipTests
print_success "Packaging completed"

# Step 4: Deploy Avro schemas (if not app-only)
if [ "$APP_ONLY" != "true" ]; then
    print_step "Deploying Avro schemas to Nexus"
    cd modules/avro
    
    # Deploy Avro module with sources and javadoc
    mvn clean deploy -Davro.publish.enabled=true -DskipTests
    
    cd ../..
    print_success "Avro schemas deployed to Nexus"
fi

# Step 5: Deploy application (if not avro-only)
if [ "$AVRO_ONLY" != "true" ]; then
    print_step "Deploying application to Nexus"
    
    # Deploy only the application module (which contains the fat JAR)
    mvn clean deploy -DskipTests -Davro.publish.enabled=false -pl modules/application
    
    print_success "Application deployed to Nexus"
fi

# Step 6: Verify deployment
print_step "Verifying deployment"

# Check if JAR files exist
if [ -f "modules/application/target/application-${CURRENT_VERSION}.jar" ]; then
    print_success "Application JAR verified: application-${CURRENT_VERSION}.jar"
else
    print_error "Application JAR not found!"
    exit 1
fi

if [ -f "modules/avro/target/avro-schemas-${CURRENT_VERSION}.jar" ]; then
    print_success "Avro JAR verified: avro-schemas-${CURRENT_VERSION}.jar"
else
    print_warning "Avro JAR not found (this might be normal if no Avro schemas exist)"
fi

print_success "Deployment completed successfully!"
echo ""
echo "📋 Deployed artifacts:"
echo "  - Application JAR: application-${CURRENT_VERSION}.jar"
echo "  - Avro JAR: avro-schemas-${CURRENT_VERSION}.jar"
echo ""
echo "🌐 Nexus Repository: ${NEXUS_BASE_URL}"
if [ "$IS_SNAPSHOT" = "true" ]; then
    echo "📦 Repository: maven-snapshots"
else
    echo "📦 Repository: maven-releases"
fi 