#!/bin/bash

# Deploy with Version Script for Multi-Module Maven Project
# Usage: ./deploy-with-version.sh <version> [options]
# Example: ./deploy-with-version.sh 1.0.0
# Example: ./deploy-with-version.sh 1.0.0-SNAPSHOT --avro-only

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
SKIP_TESTS=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --avro-only)
            AVRO_ONLY=true
            shift
            ;;
        --app-only)
            APP_ONLY=true
            shift
            ;;
        --skip-tests)
            SKIP_TESTS=true
            shift
            ;;
        --help)
            echo "Usage: $0 <version> [OPTIONS]"
            echo ""
            echo "Arguments:"
            echo "  <version>              Version to deploy (e.g., 1.0.0 or 1.0.0-SNAPSHOT)"
            echo ""
            echo "Options:"
            echo "  --avro-only           Deploy only Avro schemas"
            echo "  --app-only            Deploy only application (skip Avro)"
            echo "  --skip-tests          Skip running tests"
            echo "  --help                Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0 1.0.0                                    # Deploy release version 1.0.0"
            echo "  $0 1.0.0-SNAPSHOT                          # Deploy snapshot version"
            echo "  $0 1.0.0 --avro-only                       # Deploy only Avro schemas"
            echo "  $0 1.0.0 --app-only --skip-tests           # Deploy only app, skip tests"
            exit 0
            ;;
        *)
            if [ -z "$DEPLOY_VERSION" ]; then
                DEPLOY_VERSION="$1"
            else
                print_error "Unknown option: $1"
                echo "Use --help for usage information"
                exit 1
            fi
            shift
            ;;
    esac
done

# Validate version argument
if [ -z "$DEPLOY_VERSION" ]; then
    print_error "Version argument is required"
    echo "Usage: $0 <version> [OPTIONS]"
    echo "Example: $0 1.0.0"
    exit 1
fi

# Validate version format
if [[ ! "$DEPLOY_VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+(-SNAPSHOT)?$ ]]; then
    print_error "Invalid version format: $DEPLOY_VERSION"
    echo "Version must be in format: X.Y.Z or X.Y.Z-SNAPSHOT"
    echo "Examples: 1.0.0, 1.0.0-SNAPSHOT, 2.1.3"
    exit 1
fi

# Check if Maven wrapper exists
if [ ! -f "./mvnw" ]; then
    print_error "Maven wrapper (mvnw) not found in current directory"
    echo "Please run this script from the project root directory"
    exit 1
fi

# Make mvnw executable
chmod +x ./mvnw

# Check Nexus credentials
if [ -z "$NEXUS_USERNAME" ] || [ -z "$NEXUS_PASSWORD" ] || [ -z "$NEXUS_BASE_URL" ]; then
    print_error "Nexus credentials not set. Please set the following environment variables:"
    print_error "  NEXUS_USERNAME"
    print_error "  NEXUS_PASSWORD" 
    print_error "  NEXUS_BASE_URL"
    exit 1
fi

print_step "Deploying version: $DEPLOY_VERSION"

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

# Determine if this is a snapshot or release
IS_SNAPSHOT=false
if echo "$DEPLOY_VERSION" | grep -q "SNAPSHOT"; then
    IS_SNAPSHOT=true
    print_warning "Deploying SNAPSHOT version to snapshot repository"
else
    print_warning "Deploying RELEASE version to release repository"
fi

# Step 1: Update all module versions consistently
print_step "Updating project versions to: $DEPLOY_VERSION"

# Update root POM version
./mvnw versions:set -DnewVersion="$DEPLOY_VERSION" -DgenerateBackupPoms=false

# Update Avro module version (standalone)
./mvnw versions:set -DnewVersion="$DEPLOY_VERSION" -DgenerateBackupPoms=false -pl modules/avro

# Update all other modules
./mvnw versions:set -DnewVersion="$DEPLOY_VERSION" -DgenerateBackupPoms=false -pl modules/commons,modules/domain,modules/port-in,modules/port-out,modules/adapter-in,modules/adapter-out,modules/application

print_success "All module versions updated to $DEPLOY_VERSION"

# Step 2: Clean and compile
print_step "Cleaning and compiling project"
if [ "$SKIP_TESTS" = "true" ]; then
    ./mvnw clean compile -DskipTests
else
    ./mvnw clean compile
fi
print_success "Compilation completed"

# Step 3: Run tests (if not skipped)
if [ "$SKIP_TESTS" != "true" ]; then
    print_step "Running tests"
    ./mvnw test
    print_success "Tests completed"
else
    print_warning "Tests skipped"
fi

# Step 4: Package
print_step "Packaging application"
if [ "$SKIP_TESTS" = "true" ]; then
    ./mvnw package -DskipTests
else
    ./mvnw package
fi
print_success "Packaging completed"

# Step 5: Deploy Avro schemas (if not app-only)
if [ "$APP_ONLY" != "true" ]; then
    print_step "Deploying Avro schemas to Nexus"
    cd modules/avro
    
    # Deploy Avro module with sources and javadoc
    if [ "$SKIP_TESTS" = "true" ]; then
        ../../mvnw clean deploy -Davro.publish.enabled=true -DskipTests
    else
        ../../mvnw clean deploy -Davro.publish.enabled=true
    fi
    
    cd ../..
    print_success "Avro schemas deployed to Nexus"
fi

# Step 6: Deploy application (if not avro-only)
if [ "$AVRO_ONLY" != "true" ]; then
    print_step "Deploying application to Nexus"
    
    # Deploy only the application module (which contains the fat JAR)
    if [ "$SKIP_TESTS" = "true" ]; then
        ./mvnw clean deploy -DskipTests -Davro.publish.enabled=false -pl modules/application
    else
        ./mvnw clean deploy -Davro.publish.enabled=false -pl modules/application
    fi
    
    print_success "Application deployed to Nexus"
fi

# Step 7: Verify deployment
print_step "Verifying deployment"

# Check if JAR files exist
if [ -f "modules/application/target/application-${DEPLOY_VERSION}.jar" ]; then
    print_success "Application JAR verified: application-${DEPLOY_VERSION}.jar"
else
    print_error "Application JAR not found!"
    exit 1
fi

if [ -f "modules/avro/target/avro-schemas-${DEPLOY_VERSION}.jar" ]; then
    print_success "Avro JAR verified: avro-schemas-${DEPLOY_VERSION}.jar"
else
    print_warning "Avro JAR not found (this might be normal if no Avro schemas exist)"
fi

print_success "Deployment completed successfully!"
echo ""
echo "📋 Deployed artifacts:"
echo "  - Application JAR: application-${DEPLOY_VERSION}.jar"
echo "  - Avro JAR: avro-schemas-${DEPLOY_VERSION}.jar"
echo ""
echo "🌐 Nexus Repository: ${NEXUS_BASE_URL}"
if [ "$IS_SNAPSHOT" = "true" ]; then
    echo "📦 Repository: maven-snapshots"
else
    echo "📦 Repository: maven-releases"
fi
echo ""
echo "💡 Next steps:"
echo "1. Review the changes: git diff"
echo "2. Commit the version update: git add . && git commit -m 'Update version to $DEPLOY_VERSION'"
if [ "$IS_SNAPSHOT" != "true" ]; then
    echo "3. Create release tag: git tag v$DEPLOY_VERSION && git push origin v$DEPLOY_VERSION"
fi 