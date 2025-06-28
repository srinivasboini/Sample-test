#!/bin/bash

# Version Management Script for Sample Test Project
# Usage: ./update-version.sh <new-version>

set -e

if [ $# -eq 0 ]; then
    echo "Usage: $0 <new-version>"
    echo "Example: $0 1.0.0"
    echo "Example: $0 1.0.0-SNAPSHOT"
    exit 1
fi

NEW_VERSION=$1

echo "🔄 Updating project version to: $NEW_VERSION"

# Update root POM version
echo "📝 Updating root POM version..."
mvn versions:set -DnewVersion="$NEW_VERSION" -DgenerateBackupPoms=false

# Update Avro module version (standalone)
echo "📝 Updating Avro module version..."
mvn versions:set -DnewVersion="$NEW_VERSION" -DgenerateBackupPoms=false -pl modules/avro

# Update all other modules
echo "📝 Updating all module versions..."
mvn versions:set -DnewVersion="$NEW_VERSION" -DgenerateBackupPoms=false -pl modules/commons,modules/domain,modules/port-in,modules/port-out,modules/adapter-in,modules/adapter-out,modules/application

echo "✅ Version updated to $NEW_VERSION across all modules"
echo ""
echo "📋 Next steps:"
echo "1. Review the changes: git diff"
echo "2. Commit the version update: git add . && git commit -m 'Update version to $NEW_VERSION'"
echo "3. For releases: git tag v$NEW_VERSION && git push origin v$NEW_VERSION" 