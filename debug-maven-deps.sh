#!/bin/bash

# Debug Maven Dependencies Script
# This script helps identify and resolve Maven dependency issues in CI environments

set -e

echo "🔍 Maven Dependency Debug Script"
echo "=================================="

# Check if we're in a CI environment
if [ -n "$CI" ]; then
    echo "✅ Running in CI environment"
else
    echo "ℹ️  Running in local environment"
fi

# Display Maven version and settings
echo ""
echo "📋 Maven Information:"
echo "Maven version: $(mvn --version | head -1)"
echo "Maven home: $MAVEN_HOME"
echo "Maven opts: $MAVEN_OPTS"

# Check if settings file exists
if [ -f "maven-settings.xml" ]; then
    echo "✅ Found maven-settings.xml"
else
    echo "⚠️  maven-settings.xml not found"
fi

# Display project structure
echo ""
echo "📁 Project Structure:"
echo "Current directory: $(pwd)"
echo "Parent POM: $(find . -name "pom.xml" -maxdepth 1)"
echo "Module POMs:"
find modules -name "pom.xml" 2>/dev/null || echo "No modules directory found"

# Check module order in parent POM
echo ""
echo "📦 Module Order Check:"
if [ -f "pom.xml" ]; then
    echo "Modules defined in parent POM:"
    grep -A 20 "<modules>" pom.xml | grep "<module>" | sed 's/.*<module>\(.*\)<\/module>.*/  - \1/'
else
    echo "❌ Parent POM not found"
fi

# Try to resolve dependencies for each module
echo ""
echo "🔧 Dependency Resolution Test:"
for module in commons avro domain port-in port-out adapter-in adapter-out application; do
    if [ -d "modules/$module" ]; then
        echo "Testing module: $module"
        cd "modules/$module"
        if mvn dependency:resolve --settings ../../maven-settings.xml --batch-mode --quiet 2>/dev/null; then
            echo "  ✅ $module: Dependencies resolved successfully"
        else
            echo "  ❌ $module: Dependency resolution failed"
        fi
        cd ../..
    fi
done

# Check local repository
echo ""
echo "🏠 Local Repository Check:"
if [ -n "$MAVEN_OPTS" ] && echo "$MAVEN_OPTS" | grep -q "maven.repo.local"; then
    REPO_PATH=$(echo "$MAVEN_OPTS" | sed 's/.*maven.repo.local=\([^ ]*\).*/\1/')
    echo "Local repository path: $REPO_PATH"
    if [ -d "$REPO_PATH" ]; then
        echo "✅ Local repository exists"
        echo "Repository size: $(du -sh "$REPO_PATH" 2>/dev/null || echo 'unknown')"
    else
        echo "❌ Local repository does not exist"
    fi
else
    echo "ℹ️  Using default Maven local repository"
fi

# Test full build
echo ""
echo "🚀 Testing Full Build:"
if mvn clean compile --settings maven-settings.xml --batch-mode --quiet; then
    echo "✅ Full build successful"
else
    echo "❌ Full build failed"
    echo ""
    echo "🔧 Suggested fixes:"
    echo "1. Ensure modules are in correct dependency order in parent pom.xml"
    echo "2. Run 'mvn clean install -DskipTests' to install all modules locally"
    echo "3. Check that all required repositories are accessible"
    echo "4. Verify that no modules have circular dependencies"
fi

echo ""
echo "✨ Debug script completed" 