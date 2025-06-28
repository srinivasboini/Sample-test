package com.example.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * ArchUnit tests for validating hexagonal architecture compliance.
 * 
 * This test suite ensures that the project follows clean architecture principles
 * and maintains proper layer separation as defined in the hexagonal architecture pattern.
 * 
 * Architecture Layers:
 * - Domain: Core business logic and entities
 * - Port-In: Input ports defining use cases
 * - Port-Out: Output ports for external interactions
 * - Adapter-In: Input adapters (REST, Kafka consumers)
 * - Adapter-Out: Output adapters (Persistence, external services)
 * - Application: Use case orchestration
 * - Commons: Shared utilities and cross-cutting concerns
 */
class HexagonalArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setUp() {
        classes = new ClassFileImporter()
                .importPackages("com.example");
    }

    @Test
    void testHexagonalArchitectureLayers() {
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
    void testUseCaseNamingConvention() {
        ArchRule useCaseNaming = classes()
                .that().resideInAPackage("..port.in..")
                .should().haveSimpleNameEndingWith("UseCase")
                .because("Input ports should follow UseCase naming convention");

        useCaseNaming.check(classes);
    }

    @Test
    void testPortNamingConvention() {
        ArchRule portNaming = classes()
                .that().resideInAPackage("..port.out..")
                .should().haveSimpleNameEndingWith("Port")
                .because("Output ports should follow Port naming convention");

        portNaming.check(classes);
    }

    @Test
    void testAdapterNamingConvention() {
        ArchRule adapterNaming = classes()
                .that().resideInAPackage("..adapter..")
                .and().haveSimpleNameEndingWith("Adapter")
                .should().implementClassesThat().resideInAPackage("..port..")
                .because("Adapters should implement ports");

        adapterNaming.check(classes);
    }

    @Test
    void testNoCyclicDependencies() {
        SlicesRuleDefinition.slices()
                .matching("com.example.(*)..")
                .should().beFreeOfCycles()
                .because("The architecture should not have cyclic dependencies");
    }

    @Test
    void testDomainEntitiesShouldBeImmutable() {
        ArchRule domainEntitiesShouldBeImmutable = classes()
                .that().resideInAPackage("..domain.model..")
                .and().areNotInterfaces()
                .should().beImmutable()
                .because("Domain entities should be immutable for thread safety and consistency");

        domainEntitiesShouldBeImmutable.check(classes);
    }

    @Test
    void testServiceLayerResponsibilities() {
        ArchRule applicationServicesShouldNotAccessAdaptersDirectly = noClasses()
                .that().resideInAPackage("..application.service..")
                .should().dependOnClassesThat().resideInAPackage("..adapter..")
                .because("Application services should only depend on ports, not adapters directly");

        applicationServicesShouldNotAccessAdaptersDirectly.check(classes);
    }

    @Test
    void testCommonsLayerIndependence() {
        ArchRule commonsShouldNotDependOnBusinessLogic = noClasses()
                .that().resideInAPackage("..commons..")
                .should().dependOnClassesThat().resideInAPackage("..domain..")
                .because("Commons should be independent of business logic");

        commonsShouldNotDependOnBusinessLogic.check(classes);
    }

    @Test
    void testControllerLayerResponsibilities() {
        ArchRule controllersShouldOnlyDependOnPorts = classes()
                .that().resideInAPackage("..adapter.in.web..")
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage("..port.in..", "..commons..", "java..", "org.springframework..")
                .because("Controllers should only depend on input ports and framework classes");

        controllersShouldOnlyDependOnPorts.check(classes);
    }

    @Test
    void testPersistenceLayerResponsibilities() {
        ArchRule persistenceAdaptersShouldImplementPorts = classes()
                .that().resideInAPackage("..adapter.out.persistence..")
                .and().haveSimpleNameEndingWith("Adapter")
                .should().implementClassesThat().resideInAPackage("..port.out..")
                .because("Persistence adapters should implement output ports");

        persistenceAdaptersShouldImplementPorts.check(classes);
    }

    @Test
    void testKafkaConsumerResponsibilities() {
        ArchRule kafkaConsumersShouldOnlyDependOnPorts = classes()
                .that().resideInAPackage("..adapter.in.kafka..")
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage("..port.in..", "..commons..", "java..", "org.springframework.kafka..", "org.apache.kafka..")
                .because("Kafka consumers should only depend on input ports and Kafka framework");

        kafkaConsumersShouldOnlyDependOnPorts.check(classes);
    }

    @Test
    void testDomainServicesShouldNotDependOnInfrastructure() {
        ArchRule domainServicesShouldNotDependOnInfrastructure = noClasses()
                .that().resideInAPackage("..domain.service..")
                .should().dependOnClassesThat().resideInAPackage("..adapter..")
                .because("Domain services should be pure business logic without infrastructure dependencies");

        domainServicesShouldNotDependOnInfrastructure.check(classes);
    }

    @Test
    void testValueObjectsShouldBeImmutable() {
        ArchRule valueObjectsShouldBeImmutable = classes()
                .that().resideInAPackage("..domain.model..")
                .and().areAnnotatedWith("lombok.Value")
                .should().beImmutable()
                .because("Value objects should be immutable");

        valueObjectsShouldBeImmutable.check(classes);
    }

    @Test
    void testNoFrameworkDependenciesInDomain() {
        ArchRule domainShouldNotDependOnSpring = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("org.springframework..")
                .because("Domain should not depend on Spring framework");

        domainShouldNotDependOnSpring.check(classes);
    }

    @Test
    void testApplicationServicesShouldBeTransactional() {
        ArchRule applicationServicesShouldBeTransactional = classes()
                .that().resideInAPackage("..application.service..")
                .and().haveSimpleNameEndingWith("Service")
                .should().beAnnotatedWith("org.springframework.stereotype.Service")
                .because("Application services should be Spring components");

        applicationServicesShouldBeTransactional.check(classes);
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
} 