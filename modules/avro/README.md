# Avro Schemas Module - Standalone

This module contains Avro schema definitions and generated classes for the Sample Test project. It can be used both as part of the parent project and as a completely standalone module.

## Standalone Usage

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Building Standalone

You can build this module independently without the parent project:

```bash
# From the avro module directory
cd modules/avro

# Clean and build
mvn clean install

# Generate standalone POM (removes parent dependency)
mvn flatten:flatten
```

Or use the provided build script from the project root:

```bash
# From the project root
./build-avro-standalone.sh
```

### Using as a Dependency

Once built, you can use this module as a dependency in other projects:

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>avro-schemas</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Generated Files

After building, you'll find these files in the `target/` directory:

- `avro-schemas-1.0.0-SNAPSHOT.jar` - Main JAR with compiled classes
- `avro-schemas-1.0.0-SNAPSHOT-sources.jar` - Source JAR
- `avro-schemas-1.0.0-SNAPSHOT-javadoc.jar` - Javadoc JAR
- `avro-schemas-1.0.0-SNAPSHOT.pom` - Standalone POM (no parent dependency)

## Schema Management

### Adding New Schemas

1. Add your `.avsc` files to `src/main/avro/`
2. Run `mvn generate-sources` to generate Java classes
3. The generated classes will be in `src/main/java/`

### Schema Evolution

When modifying existing schemas:
1. Update the `.avsc` file
2. Regenerate classes: `mvn generate-sources`
3. Update version if needed
4. Test compatibility with existing data

## Publishing

To publish to a Maven repository:

```bash
# Enable publishing
mvn deploy -Davro.publish.enabled=true

# Or use the publish profile
mvn deploy -P publish-avro
```

## Integration with Parent Project

When used within the parent project:
- The module is managed by the parent POM
- Version is controlled by `avro.schema.version` property
- Builds as part of the multi-module project

## Configuration

Key properties in `pom.xml`:

- `avro.version` - Avro library version
- `avro.publish.enabled` - Controls publishing behavior
- `java.version` - Java version for compilation

## Troubleshooting

### Common Issues

1. **Schema compilation errors**: Check your `.avsc` files for syntax errors
2. **Version conflicts**: Ensure consistent Avro versions across dependencies
3. **Build failures**: Run `mvn clean` before rebuilding

### Getting Help

- Check the generated Java classes for compilation errors
- Verify schema syntax with Avro tools
- Review Maven build logs for detailed error messages 