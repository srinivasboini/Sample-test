#!/bin/bash

# Quick Deploy Script - Deploy Fat JAR Only
# Usage: ./quick-deploy.sh <version>
# Example: ./quick-deploy.sh 1.0.0

VERSION=$1

if [ -z "$VERSION" ]; then
    echo "Usage: $0 <version>"
    echo "Example: $0 1.0.0"
    exit 1
fi

echo "🚀 Quick deploy version: $VERSION"

# Make mvnw executable and deploy
chmod +x ./mvnw
./mvnw versions:set -DnewVersion="$VERSION" -DgenerateBackupPoms=false
./mvnw versions:set -DnewVersion="$VERSION" -DgenerateBackupPoms=false -pl modules/avro
./mvnw clean install -DskipTests
./mvnw deploy -DskipTests -Davro.publish.enabled=false -pl modules/application

echo "✅ Fat JAR deployed: application-${VERSION}.jar" 