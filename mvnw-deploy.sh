#!/bin/bash

# Simple Maven Wrapper Deploy Script - Deploy Only Application Fat JAR
# Usage: ./mvnw-deploy.sh <version>
# Example: ./mvnw-deploy.sh 1.0.0

set -e

VERSION=$1

if [ -z "$VERSION" ]; then
    echo "Usage: $0 <version>"
    echo "Example: $0 1.0.0"
    echo "Example: $0 1.0.0-SNAPSHOT"
    exit 1
fi

echo "🚀 Deploying version: $VERSION"

# Make mvnw executable
chmod +x ./mvnw

# Step 1: Update all versions (needed for proper dependency resolution)
echo "📝 Updating project versions..."
./mvnw versions:set -DnewVersion="$VERSION" -DgenerateBackupPoms=false
./mvnw versions:set -DnewVersion="$VERSION" -DgenerateBackupPoms=false -pl modules/avro

# Step 2: Build all modules (needed for the fat JAR)
echo "🔨 Building all modules..."
./mvnw clean install -DskipTests

# Step 3: Deploy only the application module (fat JAR)
echo "📦 Deploying application fat JAR..."
./mvnw deploy -DskipTests -Davro.publish.enabled=false -pl modules/application

echo "✅ Deployment completed for version: $VERSION"
echo ""
echo "📋 Deployed artifact:"
echo "  - Application Fat JAR: application-${VERSION}.jar"
echo ""
echo "💡 This fat JAR contains all modules and dependencies" 