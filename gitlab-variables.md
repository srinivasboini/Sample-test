# GitLab CI/CD Variables Configuration

This document lists all the variables that need to be configured in your GitLab project settings for the CI/CD pipeline to work properly.

## Required Variables

### Nexus Repository Configuration
- `NEXUS_BASE_URL`: The base URL of your Nexus repository (e.g., `https://nexus.yourcompany.com`)
- `NEXUS_USERNAME`: Username for Nexus authentication
- `NEXUS_PASSWORD`: Password for Nexus authentication (should be marked as masked/protected)

### Kubernetes Configuration (if using Kubernetes deployment)
- `CLUSTER_DOMAIN`: Your cluster domain for application URLs (e.g., `k8s.yourcompany.com`)
- `KUBECONFIG`: Kubernetes configuration file content (should be marked as masked/protected)

### Docker Registry (Uses GitLab's built-in registry by default)
- `CI_REGISTRY`: GitLab container registry URL (automatically provided by GitLab)
- `CI_REGISTRY_USER`: GitLab registry username (automatically provided by GitLab)
- `CI_REGISTRY_PASSWORD`: GitLab registry password (automatically provided by GitLab)

## Avro Schema Management Variables

### Required for Avro Publishing
- `AVRO_SCHEMA_VERSION`: Version to use for Avro schema releases (e.g., `1.2.0`)
  - Used only when manually publishing Avro schemas
  - Should NOT contain "SNAPSHOT" for release publishing
  - Example: `1.2.0`, `2.0.0`, `1.5.1`

### Optional Avro Configuration
- `CREATE_AVRO_TAG`: Create Git tag when publishing Avro releases (default: `false`)
  - Set to `true` to automatically create Git tags like `avro-v1.2.0`
  - Requires CI to have push permissions to the repository

## Optional Variables

### Application Configuration
- `SPRING_PROFILES_ACTIVE`: Override default Spring profiles for different environments
- `DATABASE_URL`: Override database connection URL
- `DATABASE_USERNAME`: Override database username
- `DATABASE_PASSWORD`: Override database password

## How to Set Variables

1. Go to your GitLab project
2. Navigate to **Settings** > **CI/CD**
3. Expand the **Variables** section
4. Click **Add variable** for each variable listed above
5. Mark sensitive variables (passwords, tokens) as **Masked** and **Protected**

## Avro Publishing Control

### Master Control Variable
- `PUBLISH_AVRO_SCHEMAS`: Master switch for Avro schema publishing (default: `false`)
  - Set to `true` to enable automatic Avro publishing on main branch
  - Set to `false` to require manual triggers only
  - Can be overridden per-job using job variables

### Manual Publishing (Default Behavior)
When `PUBLISH_AVRO_SCHEMAS=false` (default), Avro schemas are NOT published automatically.

#### To Publish Avro Snapshots:
1. **Option A - Manual Job Trigger:**
   - Go to **CI/CD** → **Pipelines**
   - Find your pipeline 
   - Click the ▶️ button next to `publish-avro-snapshot`

2. **Option B - Enable and Push:**
   - Set `PUBLISH_AVRO_SCHEMAS=true` in GitLab variables
   - Push to main branch (will auto-publish snapshot)

3. **Option C - Job-Level Override:**
   - Trigger job manually with variable override
   - Set `PUBLISH_AVRO_SCHEMAS=true` as job variable

#### To Publish Avro Releases:
1. **Prerequisites:**
   - Set `AVRO_SCHEMA_VERSION` variable (e.g., `1.2.0`)
   - Optionally set `CREATE_AVRO_TAG=true`

2. **Option A - Manual Job Trigger:**
   - Go to **CI/CD** → **Pipelines**
   - Click the ▶️ button next to `publish-avro-release`

3. **Option B - Enable and Push:**
   - Set `PUBLISH_AVRO_SCHEMAS=true` 
   - Set `AVRO_SCHEMA_VERSION=1.2.0`
   - Push to main branch (will auto-publish release)

### Automatic Publishing (When Enabled)
When `PUBLISH_AVRO_SCHEMAS=true`, the pipeline will:
- **Snapshot**: Auto-publish on every main branch push
- **Release**: Auto-publish when `AVRO_SCHEMA_VERSION` is also set

### Commit Message Triggers
These work regardless of `PUBLISH_AVRO_SCHEMAS` setting:

```bash
# Publish snapshot
git commit -m "Update user schema [publish-avro-snapshot]"

# Publish release (version specified in message)
git commit -m "Release payment schema [release-avro:1.2.0]"
```

## Environment-Specific Variables

For different environments (dev, staging, production), you can set environment-specific variables by:

1. Creating separate variable keys for each environment:
   - `DEV_DATABASE_URL`
   - `STAGING_DATABASE_URL`
   - `PROD_DATABASE_URL`

2. Or using GitLab environments feature to scope variables to specific environments.

## Kubernetes Secrets

If deploying to Kubernetes, ensure the following secrets are created in your cluster:

### Development Environment
```bash
kubectl create secret generic app-secrets -n sample-test-dev \
  --from-literal=database-url="jdbc:postgresql://postgres:5432/sample_db_dev" \
  --from-literal=database-username="postgres" \
  --from-literal=database-password="password"
```

### Staging Environment
```bash
kubectl create secret generic app-secrets-staging -n sample-test-staging \
  --from-literal=database-url="jdbc:postgresql://postgres:5432/sample_db_staging" \
  --from-literal=database-username="postgres" \
  --from-literal=database-password="password"
```

### Production Environment
```bash
kubectl create secret generic app-secrets-prod -n sample-test-prod \
  --from-literal=database-url="jdbc:postgresql://postgres:5432/sample_db_prod" \
  --from-literal=database-username="postgres" \
  --from-literal=database-password="secure_password"
```

## Variable Examples

Here's a complete example of variables you might set:

| Variable | Value | Type | Protected |
|----------|-------|------|-----------|
| `NEXUS_BASE_URL` | `https://nexus.yourcompany.com` | Variable | No |
| `NEXUS_USERNAME` | `ci-deploy` | Variable | No |
| `NEXUS_PASSWORD` | `secure_password_123` | Variable | Yes |
| `AVRO_SCHEMA_VERSION` | `1.0.0` | Variable | No |
| `CREATE_AVRO_TAG` | `true` | Variable | No |
| `CLUSTER_DOMAIN` | `k8s.yourcompany.com` | Variable | No |

## Notes

- **Avro schemas are NOT published automatically** - you control when they're published
- Application artifacts are still published automatically on tagged releases
- Docker images are built and pushed to GitLab's container registry
- Deployments are manual and require approval for each environment
- Avro schemas have independent versioning from the main application 