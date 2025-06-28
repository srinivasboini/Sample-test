# GitLab CI/CD Variables Configuration

This document lists all the required and optional variables for the GitLab CI/CD pipeline.

## Required Variables

### Nexus Repository Configuration
These variables are **required** for deploying artifacts to Nexus:

| Variable | Description | Example |
|----------|-------------|---------|
| `NEXUS_USERNAME` | Username for Nexus authentication | `deploy-user` |
| `NEXUS_PASSWORD` | Password for Nexus authentication | `your-secure-password` |
| `NEXUS_BASE_URL` | Base URL of your Nexus server | `https://nexus.company.com` |

### GitLab Container Registry
These are automatically provided by GitLab but can be overridden:

| Variable | Description | Default |
|----------|-------------|---------|
| `CI_REGISTRY` | GitLab Container Registry URL | `registry.gitlab.com` |
| `CI_REGISTRY_USER` | Registry username | GitLab username |
| `CI_REGISTRY_PASSWORD` | Registry password | GitLab token |

## Optional Variables

### Deployment Control
These variables control deployment behavior:

| Variable | Description | Default | Values |
|----------|-------------|---------|--------|
| `DEPLOY_VERSION` | Version to deploy (overrides POM version) | POM version | `1.0.0`, `1.0.0-SNAPSHOT` |
| `PUBLISH_AVRO_SCHEMAS` | Enable Avro schema publishing | `false` | `true`, `false` |
| `AVRO_SCHEMA_VERSION` | Version for Avro schemas (if different from app) | App version | `1.0.0`, `1.0.0-SNAPSHOT` |
| `CREATE_AVRO_TAG` | Create Git tag for Avro releases | `false` | `true`, `false` |

### Kubernetes Deployment
These variables are used for Kubernetes deployments:

| Variable | Description | Default | Example |
|----------|-------------|---------|---------|
| `CLUSTER_DOMAIN` | Kubernetes cluster domain | `cluster.local` | `k8s.company.com` |
| `KUBE_CONFIG` | Base64 encoded kubeconfig | - | Base64 string |

## How to Set Variables

### Method 1: GitLab Project Variables (Recommended)
1. Go to your GitLab project
2. Navigate to **Settings** → **CI/CD**
3. Expand **Variables** section
4. Add each variable:
   - **Key**: Variable name (e.g., `NEXUS_USERNAME`)
   - **Value**: Variable value
   - **Type**: Variable (default) or File
   - **Environment scope**: All (default) or specific environment
   - **Protect variable**: Check if it should only be available in protected branches
   - **Mask variable**: Check to hide the value in job logs

### Method 2: GitLab Group Variables
For variables shared across multiple projects:
1. Go to your GitLab group
2. Navigate to **Settings** → **CI/CD**
3. Expand **Variables** section
4. Add variables (same process as project variables)

### Method 3: GitLab Instance Variables
For organization-wide variables:
1. Go to **Admin Area** → **Settings** → **CI/CD**
2. Expand **Variables** section
3. Add variables

## Security Best Practices

### 1. Protect Sensitive Variables
Always check **Protect variable** for:
- `NEXUS_PASSWORD`
- `KUBE_CONFIG`
- Any API keys or tokens

### 2. Mask Sensitive Values
Check **Mask variable** for:
- Passwords
- Tokens
- API keys
- Any sensitive configuration

### 3. Environment Scoping
Use environment scoping for:
- Development-specific configurations
- Production-specific secrets
- Environment-specific URLs

## Variable Usage Examples

### Deploy Specific Version
```bash
# Set in GitLab CI/CD variables
DEPLOY_VERSION=1.2.0
```

### Enable Avro Publishing
```bash
# Set in GitLab CI/CD variables
PUBLISH_AVRO_SCHEMAS=true
AVRO_SCHEMA_VERSION=1.1.0
```

### Deploy to Specific Environment
```bash
# Set in GitLab CI/CD variables with environment scope
ENVIRONMENT=production
CLUSTER_DOMAIN=prod.k8s.company.com
```

## Troubleshooting

### Common Issues

1. **Nexus Authentication Failed**
   - Verify `NEXUS_USERNAME` and `NEXUS_PASSWORD` are correct
   - Check if user has deploy permissions
   - Ensure `NEXUS_BASE_URL` is accessible

2. **Version Not Updated**
   - Check if `DEPLOY_VERSION` is set correctly
   - Verify the variable is not protected for your branch
   - Ensure the version format is valid (e.g., `1.0.0` or `1.0.0-SNAPSHOT`)

3. **Avro Publishing Skipped**
   - Set `PUBLISH_AVRO_SCHEMAS=true`
   - Check if `AVRO_SCHEMA_VERSION` is set (for releases)
   - Verify Avro module exists and has schemas

4. **Docker Build Failed**
   - Check `CI_REGISTRY_*` variables are set
   - Verify registry permissions
   - Ensure JAR files are created in package stage

### Debugging Tips

1. **Check Variable Values**
   ```bash
   # Add to your CI script to debug
   echo "NEXUS_BASE_URL: $NEXUS_BASE_URL"
   echo "DEPLOY_VERSION: $DEPLOY_VERSION"
   ```

2. **Verify File Existence**
   ```bash
   # Check if JAR files exist
   ls -la modules/application/target/
   ls -la modules/avro/target/
   ```

3. **Test Maven Commands Locally**
   ```bash
   # Test with your variables
   export NEXUS_USERNAME=your-username
   export NEXUS_PASSWORD=your-password
   export NEXUS_BASE_URL=your-nexus-url
   mvn clean deploy -DskipTests
   ```

## Migration from Old Configuration

If you're migrating from an older configuration:

1. **Backup Current Variables**
   - Export current variables from GitLab
   - Document any custom configurations

2. **Update Variable Names**
   - Ensure all variable names match the new configuration
   - Update any references in custom scripts

3. **Test Deployment**
   - Run a test deployment with new variables
   - Verify all artifacts are uploaded correctly

4. **Update Documentation**
   - Update team documentation
   - Share new variable requirements 