# Avro Schema Management and Publishing Guide

This document explains how to manage and publish Avro schemas in the Sample Test project with full control over versioning and publishing.

## 🎯 Overview

The Avro schema management has been redesigned to provide:
- **Independent versioning** of Avro schemas separate from the main application
- **Controlled publishing** - schemas are only published when explicitly requested
- **Version management** - proper semantic versioning for schema evolution
- **Multiple publishing modes** - snapshot and release publishing options

## 📋 Key Features

### ✅ **What Changed:**
- Avro module renamed to `avro-schemas` with independent versioning
- Publishing is **manual by default** - no automatic uploads
- Separate version property `avro.schema.version` for schema versioning
- Enhanced artifact generation (sources, javadoc)
- Git tagging support for schema releases

### ❌ **What's No Longer Automatic:**
- Avro schemas are **NOT** published on every main branch build
- You must explicitly trigger publishing when needed
- Version bumps must be managed manually

## 🔧 Configuration

### 1. **Version Management**

The Avro schemas use independent versioning controlled by the `avro.schema.version` property in the main `pom.xml`:

```xml
<properties>
    <!-- Application version -->
    <version>1.0.0-SNAPSHOT</version>
    
    <!-- Independent Avro schema version -->
    <avro.schema.version>1.0.0</avro.schema.version>
</properties>
```

### 2. **GitLab Variables**

Set these variables in your GitLab project:

| Variable | Description | Required | Example |
|----------|-------------|----------|---------|
| `AVRO_SCHEMA_VERSION` | Version for Avro schema releases | For releases | `1.2.0` |
| `CREATE_AVRO_TAG` | Create Git tag for Avro releases | Optional | `true` |
| `NEXUS_USERNAME` | Nexus authentication | Yes | `ci-user` |
| `NEXUS_PASSWORD` | Nexus authentication | Yes | `password` |
| `NEXUS_BASE_URL` | Nexus server URL | Yes | `https://nexus.company.com` |

## 🚀 Publishing Methods

### **Method 1: Master Control Variable**

The `PUBLISH_AVRO_SCHEMAS` variable acts as a master switch:

#### **Default Behavior (PUBLISH_AVRO_SCHEMAS=false):**
- Manual publishing only
- Jobs require explicit triggers
- Safety-first approach

#### **Automatic Publishing (PUBLISH_AVRO_SCHEMAS=true):**
- **Snapshot**: Auto-publishes on every main branch push
- **Release**: Auto-publishes when `AVRO_SCHEMA_VERSION` is set
- **Useful for**: Active development phases

### **Method 2: Manual Pipeline Jobs**

#### **For Snapshot Publishing:**
1. Go to **CI/CD** → **Pipelines** in GitLab
2. Find your pipeline
3. Click the **play button** ▶️ next to `publish-avro-snapshot`
4. The job will publish using version `${AVRO_SCHEMA_VERSION}-SNAPSHOT`

#### **For Release Publishing:**
1. Set the `AVRO_SCHEMA_VERSION` variable in GitLab (e.g., `1.2.0`)
2. Go to **CI/CD** → **Pipelines** in GitLab
3. Click the **play button** ▶️ next to `publish-avro-release`
4. Optionally set `CREATE_AVRO_TAG=true` to create a Git tag

### **Method 3: Commit Message Triggers**

These work regardless of the `PUBLISH_AVRO_SCHEMAS` setting:

#### **Snapshot Publishing:**
```bash
git commit -m "Update user schema [publish-avro-snapshot]"
git push origin main
```

#### **Release Publishing:**
```bash
# Set version in commit message
git commit -m "Release new payment schema [release-avro:1.2.0]"
git push origin main
```

### **Method 4: Temporary Enable/Disable**

#### **Enable for One Pipeline:**
```bash
# Push with temporary enable
git commit -m "Update schemas"
# In GitLab, run pipeline with job variable: PUBLISH_AVRO_SCHEMAS=true
```

#### **Enable Globally:**
```bash
# Set in GitLab project variables
PUBLISH_AVRO_SCHEMAS=true
git push origin main  # Will auto-publish snapshot
```

### **Method 5: Local Publishing**

#### **For Development/Testing:**
```bash
# Publish snapshot locally
cd modules/avro
mvn clean deploy -Davro.publish.enabled=true -DAVRO_SCHEMA_VERSION=1.1.0-SNAPSHOT

# Publish release locally  
mvn clean deploy -Davro.publish.enabled=true -DAVRO_SCHEMA_VERSION=1.1.0
```

## 📝 Version Management Strategy

### **Semantic Versioning for Schemas**

Follow semantic versioning for Avro schemas:

- **MAJOR** (`2.0.0`): Breaking changes (field removal, type changes)
- **MINOR** (`1.1.0`): Backward-compatible additions (new optional fields)
- **PATCH** (`1.0.1`): Backward-compatible fixes (documentation, comments)

### **Example Workflow**

1. **Development Phase:**
   ```bash
   # Work with snapshots during development
   avro.schema.version=1.2.0-SNAPSHOT
   ```

2. **Ready for Release:**
   ```bash
   # Update to release version
   avro.schema.version=1.2.0
   # Trigger release publishing
   ```

3. **Next Development Cycle:**
   ```bash
   # Bump to next snapshot
   avro.schema.version=1.3.0-SNAPSHOT
   ```

## 🏗️ Build Artifacts

Each Avro schema publication includes:

- **Main JAR**: Compiled Avro classes
- **Sources JAR**: Original `.avsc` schema files
- **Javadoc JAR**: Generated documentation
- **POM**: Dependency metadata

## 🔍 Monitoring and Verification

### **Check Publication Status**

1. **In Nexus Repository:**
   - Navigate to `com/example/avro-schemas/`
   - Verify version appears in repository
   - Check artifact completeness (jar, sources, javadoc)

2. **In GitLab:**
   - Check pipeline job logs for success/failure
   - Verify environment URLs in pipeline
   - Check artifacts in job outputs

### **Verify Schema Compatibility**

```bash
# Download and inspect published schema
mvn dependency:get -Dartifact=com.example:avro-schemas:1.2.0:jar:sources
mvn dependency:copy -Dartifact=com.example:avro-schemas:1.2.0:jar:sources -DoutputDirectory=./schemas
```

## 🛠️ Troubleshooting

### **Common Issues**

1. **Publishing Fails - Authentication Error:**
   ```
   Solution: Check NEXUS_USERNAME and NEXUS_PASSWORD variables
   ```

2. **Version Already Exists in Release Repository:**
   ```
   Solution: Increment AVRO_SCHEMA_VERSION or use snapshot publishing
   ```

3. **Manual Job Not Appearing:**
   ```
   Solution: Ensure you're on main branch and pipeline has completed package stage
   ```

4. **Git Tag Creation Fails:**
   ```
   Solution: Ensure CI has push permissions or set CREATE_AVRO_TAG=false
   ```

### **Debugging Commands**

```bash
# Check current Avro version in project
grep -r "avro.schema.version" pom.xml

# List available versions in Nexus
curl -u $NEXUS_USERNAME:$NEXUS_PASSWORD \
  "$NEXUS_BASE_URL/service/rest/v1/search?repository=maven-releases&name=avro-schemas"

# Verify local build
cd modules/avro
mvn clean package -Davro.publish.enabled=false
```

## 📚 Best Practices

### **1. Schema Evolution**
- Always test schema compatibility before releasing
- Use Confluent Schema Registry compatibility checks if available
- Document breaking changes in commit messages

### **2. Version Planning**
- Plan schema versions alongside application releases
- Use snapshot versions during active development
- Only release stable, tested schemas

### **3. Team Coordination**
- Communicate schema changes to consuming teams
- Use descriptive commit messages for schema updates
- Tag important schema releases for easy reference

### **4. Release Process**
```bash
# Recommended release workflow
1. Develop schemas with SNAPSHOT versions
2. Test compatibility with applications
3. Update avro.schema.version to release version
4. Trigger release publishing
5. Create Git tag for tracking
6. Update dependent applications
7. Bump to next SNAPSHOT version
```

## 🎁 Advanced Features

### **Conditional Publishing**

You can control publishing behavior with additional variables:

```yaml
# In .gitlab-ci.yml variables section
SKIP_AVRO_PUBLISHING: "true"  # Completely disable Avro publishing
AVRO_REPOSITORY_TYPE: "releases"  # Override repository type
```

### **Custom Repository URLs**

Override default repository URLs:

```bash
AVRO_RELEASE_URL="https://custom-nexus.com/repository/avro-releases/"
AVRO_SNAPSHOT_URL="https://custom-nexus.com/repository/avro-snapshots/"
```

This approach gives you complete control over when and how your Avro schemas are published, ensuring they only go to Nexus when you're ready! 🎯 