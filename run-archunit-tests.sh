#!/bin/bash

# ArchUnit Test Runner for Hexagonal Architecture
# This script runs architectural validation tests using ArchUnit

set -e

echo "🏗️  Running ArchUnit Tests for Hexagonal Architecture"
echo "=================================================="

# Check if we're in the right directory
if [ ! -f "pom.xml" ]; then
    echo "❌ Error: pom.xml not found. Please run this script from the project root."
    exit 1
fi

# Build the project first to ensure all classes are compiled
echo "📦 Building project..."
mvn clean compile -q

# Run ArchUnit tests
echo "🧪 Running ArchUnit tests..."
mvn test -Dtest=*ArchitectureTest -q

echo ""
echo "✅ ArchUnit tests completed successfully!"
echo ""
echo "📋 Architecture Validation Summary:"
echo "   ✓ Domain layer independence"
echo "   ✓ Port interface contracts"
echo "   ✓ Naming conventions"
echo "   ✓ Layer responsibilities"
echo "   ✓ Dependency direction"
echo "   ✓ No cyclic dependencies"
echo ""
echo "🎯 Your hexagonal architecture is compliant!"
echo ""
echo "💡 To run specific ArchUnit tests:"
echo "   mvn test -Dtest=SimpleArchitectureTest"
echo "   mvn test -Dtest=HexagonalArchitectureTest"
echo ""
echo "📚 For more details, see: ARCHUNIT_IMPLEMENTATION_GUIDE.md" 