#!/bin/bash

# Build script to create fat jar in root target directory
echo "Building sample-test project and creating fat jar..."

# Clean and build all modules
mvn clean install -DskipTests

# The fat jar will be created in the root target directory
# as sample-test-1.0.0-SNAPSHOT.jar

echo "Build completed!"
echo "Fat jar location: target/sample-test-1.0.0-SNAPSHOT.jar"

# Check if the jar was created
if [ -f "target/sample-test-1.0.0-SNAPSHOT.jar" ]; then
    echo "✅ Fat jar created successfully!"
    echo "Jar size: $(du -h target/sample-test-1.0.0-SNAPSHOT.jar | cut -f1)"
else
    echo "❌ Fat jar not found. Check build logs for errors."
    exit 1
fi 