#!/bin/bash

# Standalone Avro Module Build Script
# This script builds the Avro module independently without requiring the parent project

set -e

echo "=== Building Avro Module Standalone ==="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if we're in the right directory
if [ ! -f "modules/avro/pom.xml" ]; then
    print_error "This script must be run from the project root directory"
    exit 1
fi

# Navigate to avro module
cd modules/avro

print_status "Building Avro module in standalone mode..."

# Clean and build
print_status "Cleaning previous builds..."
mvn clean

print_status "Generating Avro classes..."
mvn generate-sources

print_status "Compiling..."
mvn compile

print_status "Running tests (if any)..."
mvn test

print_status "Creating JAR..."
mvn package

print_status "Installing to local repository..."
mvn install

print_status "Generating standalone POM..."
mvn flatten:flatten

print_status "=== Avro Module Build Complete ==="
print_status "Generated files:"
echo "  - JAR: target/avro-schemas-1.0.0-SNAPSHOT.jar"
echo "  - Sources JAR: target/avro-schemas-1.0.0-SNAPSHOT-sources.jar"
echo "  - Javadoc JAR: target/avro-schemas-1.0.0-SNAPSHOT-javadoc.jar"
echo "  - Standalone POM: target/avro-schemas-1.0.0-SNAPSHOT.pom"

# Optional: Deploy to repository (uncomment if needed)
# print_status "Deploying to repository..."
# mvn deploy -Davro.publish.enabled=true

print_status "Avro module is now ready for standalone use!" 