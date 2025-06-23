#!/bin/bash

# Build Modules Script
# This script builds all modules in the correct dependency order

set -e

echo "🏗️  Building modules in dependency order..."
echo "=========================================="

# Maven options
MAVEN_OPTS="${MAVEN_OPTS:--Dmaven.repo.local=.m2/repository}"
MAVEN_CLI_OPTS="${MAVEN_CLI_OPTS:---batch-mode --errors --fail-at-end --show-version --settings maven-settings.xml}"

# Function to build a module
build_module() {
    local module=$1
    local description=$2
    
    echo ""
    echo "🔨 Building $module module ($description)..."
    echo "----------------------------------------"
    
    if [ -d "modules/$module" ]; then
        cd "modules/$module"
        if mvn $MAVEN_CLI_OPTS clean install -DskipTests; then
            echo "✅ $module module built successfully"
        else
            echo "❌ Failed to build $module module"
            exit 1
        fi
        cd ../..
    else
        echo "⚠️  Module $module not found, skipping..."
    fi
}

# Clean everything first
echo "🧹 Cleaning previous builds..."
mvn $MAVEN_CLI_OPTS clean

# Build modules in dependency order
build_module "commons" "No internal dependencies"
build_module "avro" "No internal dependencies"
build_module "domain" "Depends on commons"
build_module "port-in" "Depends on domain and commons"
build_module "port-out" "Depends on domain and commons"
build_module "adapter-in" "Depends on port-in, commons, and avro"
build_module "adapter-out" "Depends on commons and port-out"
build_module "application" "Depends on all other modules"

# Verify the full build
echo ""
echo "🔍 Verifying full build..."
echo "------------------------"
if mvn $MAVEN_CLI_OPTS compile -DskipTests; then
    echo "✅ Full build verification successful"
else
    echo "❌ Full build verification failed"
    exit 1
fi

echo ""
echo "🎉 All modules built successfully!"
echo "📦 Artifacts are available in modules/*/target/" 