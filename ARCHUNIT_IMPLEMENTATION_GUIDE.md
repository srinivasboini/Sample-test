# ArchUnit Implementation Guide for Hexagonal Architecture

## Overview

This guide provides a comprehensive approach to implementing ArchUnit for your hexagonal architecture project. Your project is an excellent candidate for ArchUnit due to its well-structured modular design and clear separation of concerns.

## Why ArchUnit is Effective for Your Project

### Current Architecture Strengths
1. **Clear Layer Separation**: Well-defined modules (domain, port-in, port-out, adapter-in, adapter-out, application, commons)
2. **Explicit Dependencies**: Each module has clear dependency boundaries
3. **Consistent Naming**: Follows hexagonal architecture conventions
4. **Clean Architecture Principles**: Dependencies point inward toward domain

### Architecture Validation Opportunities
- **Domain Independence**: Ensure domain layer remains pure
- **Dependency Direction**: Validate dependencies flow inward
- **Layer Responsibilities**: Enforce proper separation of concerns
- **Naming Conventions**: Maintain consistent architectural patterns

## Implementation Strategy

### 1. Add ArchUnit Dependencies

Add to `modules/application/pom.xml`:

```xml
<!-- ArchUnit for architectural testing -->
<dependency>
    <groupId>com.tngtech.archunit</groupId>
    <artifactId>archunit-junit5</artifactId>
    <version>1.2.1</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>com.tngtech.archunit</groupId>
    <artifactId>archunit</artifactId>
    <version>1.2.1</version>
    <scope>test</scope>
</dependency>
```

### 2. Core ArchUnit Test Suite

Create `modules/application/src/test/java/com/example/architecture/HexagonalArchitectureTest.java`:

```java
package com.example.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

class HexagonalArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setUp() {
        classes = new ClassFileImporter()
                .importPackages("com.example");
    }

    @Test
    void testHexagonalArchitectureLayers() {
        // Define layered architecture
        Architectures.LayeredArchitecture layeredArchitecture = layeredArchitecture()
                .consideringAllDependencies()
                .layer("Domain").definedBy("..domain..")
                .layer("Port-In").definedBy("..port.in..")
                .layer("Port-Out").definedBy("..port.out..")
                .layer("Adapter-In").definedBy("..adapter.in..")
                .layer("Adapter-Out").definedBy("..adapter.out..")
                .layer("Application").definedBy("..application..")
                .layer("Commons").definedBy("..commons..")
                .layer("Avro").definedBy("..avro..")

                // Domain layer rules
                .whereLayer("Domain").mayNotBeAccessedByAnyLayer()
                .whereLayer("Domain").mayOnlyAccessLayers("Commons")

                // Port-In layer rules
                .whereLayer("Port-In").mayNotBeAccessedByAnyLayer()
                .whereLayer("Port-In").mayOnlyAccessLayers("Domain", "Commons")

                // Port-Out layer rules
                .whereLayer("Port-Out").mayNotBeAccessedByAnyLayer()
                .whereLayer("Port-Out").mayOnlyAccessLayers("Domain", "Commons")

                // Adapter-In layer rules
                .whereLayer("Adapter-In").mayOnlyAccessLayers("Port-In", "Commons", "Avro")
                .whereLayer("Adapter-In").mayNotBeAccessedByAnyLayer()

                // Adapter-Out layer rules
                .whereLayer("Adapter-Out").mayOnlyAccessLayers("Port-Out", "Domain")
                .whereLayer("Adapter-Out").mayNotBeAccessedByAnyLayer()

                // Application layer rules
                .whereLayer("Application").mayOnlyAccessLayers("Domain", "Port-In", "Port-Out", "Adapter-In", "Adapter-Out", "Commons")
                .whereLayer("Application").mayNotBeAccessedByAnyLayer()

                // Commons layer rules
                .whereLayer("Commons").mayNotBeAccessedByAnyLayer()
                .whereLayer("Commons").mayOnlyAccessLayers("Commons");

        layeredArchitecture.check(classes);
    }

    @Test
    void testDomainLayerIndependence() {
        ArchRule domainShouldNotDependOnInfrastructure = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..adapter..")
                .because("Domain layer should be independent of infrastructure concerns");

        domainShouldNotDependOnInfrastructure.check(classes);
    }

    @Test
    void testPortsShouldBeInterfaces() {
        ArchRule portsShouldBeInterfaces = classes()
                .that().resideInAPackage("..port..")
                .should().beInterfaces()
                .because("Ports should define contracts as interfaces");

        portsShouldBeInterfaces.check(classes);
    }

    @Test
    void testNamingConventions() {
        // Input ports should end with UseCase
        ArchRule useCaseNaming = classes()
                .that().resideInAPackage("..port.in..")
                .should().haveSimpleNameEndingWith("UseCase")
                .because("Input ports should follow UseCase naming convention");

        // Output ports should end with Port
        ArchRule portNaming = classes()
                .that().resideInAPackage("..port.out..")
                .should().haveSimpleNameEndingWith("Port")
                .because("Output ports should follow Port naming convention");

        useCaseNaming.check(classes);
        portNaming.check(classes);
    }

    @Test
    void testNoCyclicDependencies() {
        SlicesRuleDefinition.slices()
                .matching("com.example.(*)..")
                .should().beFreeOfCycles()
                .because("The architecture should not have cyclic dependencies");
    }
}
```

### 3. Specialized Test Suites

#### A. Dependency Rules Test
```java
@Test
void testNoDirectDatabaseAccessFromDomain() {
    ArchRule domainShouldNotAccessDatabase = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAPackage("javax.persistence..")
            .because("Domain should not have direct database dependencies");

    domainShouldNotAccessDatabase.check(classes);
}

@Test
void testNoDirectKafkaAccessFromDomain() {
    ArchRule domainShouldNotAccessKafka = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAPackage("org.apache.kafka..")
            .because("Domain should not have direct Kafka dependencies");

    domainShouldNotAccessKafka.check(classes);
}
```

#### B. Layer Responsibility Tests
```java
@Test
void testControllersShouldBeInAdapterInLayer() {
    ArchRule controllersShouldBeInAdapterIn = classes()
            .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
            .should().resideInAPackage("..adapter.in..")
            .because("Controllers should be in adapter-in layer for incoming requests");

    controllersShouldBeInAdapterIn.check(classes);
}

@Test
void testRepositoriesShouldBeInAdapterOutLayer() {
    ArchRule repositoriesShouldBeInAdapterOut = classes()
            .that().haveSimpleNameEndingWith("Repository")
            .should().resideInAPackage("..adapter.out..")
            .because("Repositories should be in adapter-out layer for persistence");

    repositoriesShouldBeInAdapterOut.check(classes);
}
```

#### C. Component Annotation Tests
```java
@Test
void testApplicationServicesShouldBeSpringComponents() {
    ArchRule applicationServicesShouldBeSpringComponents = classes()
            .that().resideInAPackage("..application.service..")
            .and().haveSimpleNameEndingWith("Service")
            .should().beAnnotatedWith("org.springframework.stereotype.Service")
            .because("Application services should be Spring components");

    applicationServicesShouldBeSpringComponents.check(classes);
}

@Test
void testAdaptersShouldBeSpringComponents() {
    ArchRule adaptersShouldBeSpringComponents = classes()
            .that().resideInAPackage("..adapter..")
            .and().haveSimpleNameEndingWith("Adapter")
            .should().beAnnotatedWith("org.springframework.stereotype.Component")
            .because("Adapters should be Spring components for dependency injection");

    adaptersShouldBeSpringComponents.check(classes);
}
```

## Key ArchUnit Rules for Your Architecture

### 1. Domain Layer Rules
- **No Infrastructure Dependencies**: Domain should not depend on Spring, JPA, Kafka, etc.
- **Pure Business Logic**: Only business rules and domain entities
- **No Framework Annotations**: Should not use @Service, @Component, etc.

### 2. Port Layer Rules
- **Interface Contracts**: All ports must be interfaces
- **Naming Conventions**: UseCase for input ports, Port for output ports
- **Domain Dependencies Only**: Can only depend on domain and commons

### 3. Adapter Layer Rules
- **Spring Components**: Must be annotated with @Component or @Service
- **Port Implementation**: Must implement appropriate ports
- **Technology Isolation**: Input adapters for incoming, output adapters for outgoing

### 4. Application Layer Rules
- **Orchestration**: Coordinates between adapters and domain
- **Transaction Management**: Handles transaction boundaries
- **Spring Components**: Must be annotated appropriately

### 5. Commons Layer Rules
- **Cross-cutting Concerns**: Utilities, MDC, async configuration
- **No Business Logic**: Should not depend on domain or ports
- **Framework Independent**: Should work across all layers

## Running ArchUnit Tests

### Maven Command
```bash
mvn test -Dtest=*ArchitectureTest
```

### IDE Integration
- Run as JUnit tests in your IDE
- Integrate with CI/CD pipeline
- Use as part of build verification

## Benefits for Your Project

### 1. Architecture Enforcement
- **Prevents Violations**: Catches architectural violations early
- **Documentation**: Tests serve as living architecture documentation
- **Refactoring Safety**: Ensures changes don't break architecture

### 2. Team Alignment
- **Shared Understanding**: Clear rules for all team members
- **Onboarding**: New developers understand architecture quickly
- **Code Reviews**: Automated validation of architectural decisions

### 3. Quality Assurance
- **Consistency**: Maintains architectural patterns across the codebase
- **Maintainability**: Prevents technical debt accumulation
- **Scalability**: Ensures architecture scales with team growth

## Custom Rules for Your Specific Needs

### 1. Kafka Integration Rules
```java
@Test
void testKafkaConsumerResponsibilities() {
    ArchRule kafkaConsumersShouldOnlyDependOnPorts = classes()
            .that().resideInAPackage("..adapter.in.kafka..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage("..port.in..", "..commons..", "java..", "org.springframework.kafka..", "org.apache.kafka..")
            .because("Kafka consumers should only depend on input ports and Kafka framework");

    kafkaConsumersShouldOnlyDependOnPorts.check(classes);
}
```

### 2. Avro Schema Rules
```java
@Test
void testAvroSchemaUsage() {
    ArchRule avroShouldOnlyBeUsedInAdapters = classes()
            .that().dependOnClassesThat().resideInAPackage("..avro..")
            .should().resideInAnyPackage("..adapter..", "..application..")
            .because("Avro schemas should only be used in adapters and application layer");

    avroShouldOnlyBeUsedInAdapters.check(classes);
}
```

### 3. MDC Integration Rules
```java
@Test
void testMDCUtilitiesLocation() {
    ArchRule mdcUtilitiesShouldBeInCommons = classes()
            .that().haveSimpleNameContaining("Mdc")
            .should().resideInAPackage("..commons..")
            .because("MDC utilities should be in commons for cross-cutting concerns");

    mdcUtilitiesShouldBeInCommons.check(classes);
}
```

## Integration with CI/CD

### 1. Maven Profile
Add to `pom.xml`:
```xml
<profile>
    <id>architecture-test</id>
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <configuration>
                    <includes>
                        <include>**/*ArchitectureTest.java</include>
                    </includes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</profile>
```

### 2. GitLab CI Integration
```yaml
architecture-test:
  stage: test
  script:
    - mvn test -P architecture-test
  rules:
    - changes:
        - "**/*.java"
```

## Best Practices

### 1. Test Organization
- **Group Related Rules**: Organize tests by architectural concern
- **Clear Naming**: Use descriptive test names
- **Comprehensive Coverage**: Test all architectural layers

### 2. Rule Maintenance
- **Regular Review**: Update rules as architecture evolves
- **Team Input**: Involve team in rule creation and maintenance
- **Documentation**: Keep rules documented and explained

### 3. Performance Considerations
- **Selective Testing**: Test only relevant packages
- **Caching**: Use ArchUnit's caching capabilities
- **CI Integration**: Run in parallel with other tests

## Conclusion

ArchUnit is highly effective for your hexagonal architecture project because:

1. **Clear Boundaries**: Your modular structure makes rule definition straightforward
2. **Consistent Patterns**: Well-established naming and dependency conventions
3. **Clean Architecture**: Strong adherence to clean architecture principles
4. **Team Benefits**: Automated validation of architectural decisions

The implementation will provide immediate value in maintaining architectural integrity and will scale with your project's growth. 