package com.example.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

/**
 * Core ArchUnit tests for hexagonal architecture validation.
 * 
 * These tests validate the fundamental principles of hexagonal architecture:
 * - Domain independence from infrastructure
 * - Proper layer separation
 * - Dependency direction (inward toward domain)
 * - Naming conventions
 */
class HexagonalArchitectureRulesTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setUp() {
        classes = new ClassFileImporter()
                .importPackages("com.example");
    }

    @Test
    void testDomainLayerIndependence() {
        // Domain should not depend on infrastructure
        ArchRule domainShouldNotDependOnAdapters = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..adapter..")
                .because("Domain layer should be independent of infrastructure concerns");

        // Domain should not depend on Spring
        ArchRule domainShouldNotDependOnSpring = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("org.springframework..")
                .because("Domain should not depend on Spring framework");

        // Domain should not depend on JPA
        ArchRule domainShouldNotDependOnJPA = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("javax.persistence..")
                .because("Domain should not have direct database dependencies");

        domainShouldNotDependOnAdapters.check(classes);
        domainShouldNotDependOnSpring.check(classes);
        domainShouldNotDependOnJPA.check(classes);
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
        ArchRule inputPortsShouldEndWithUseCase = classes()
                .that().resideInAPackage("..port.in..")
                .should().haveSimpleNameEndingWith("UseCase")
                .because("Input ports should follow UseCase naming convention");

        // Output ports should end with Port
        ArchRule outputPortsShouldEndWithPort = classes()
                .that().resideInAPackage("..port.out..")
                .should().haveSimpleNameEndingWith("Port")
                .because("Output ports should follow Port naming convention");

        // Adapters should end with Adapter
        ArchRule adaptersShouldEndWithAdapter = classes()
                .that().resideInAPackage("..adapter..")
                .and().haveSimpleNameEndingWith("Adapter")
                .should().implementClassesThat().resideInAPackage("..port..")
                .because("Adapters should implement ports");

        inputPortsShouldEndWithUseCase.check(classes);
        outputPortsShouldEndWithPort.check(classes);
        adaptersShouldEndWithAdapter.check(classes);
    }

    @Test
    void testLayerResponsibilities() {
        // Controllers should be in adapter-in layer
        ArchRule controllersShouldBeInAdapterIn = classes()
                .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
                .should().resideInAPackage("..adapter.in..")
                .because("Controllers should be in adapter-in layer for incoming requests");

        // Repositories should be in adapter-out layer
        ArchRule repositoriesShouldBeInAdapterOut = classes()
                .that().haveSimpleNameEndingWith("Repository")
                .should().resideInAPackage("..adapter.out..")
                .because("Repositories should be in adapter-out layer for persistence");

        // Mappers should be in adapter layer
        ArchRule mappersShouldBeInAdapterLayer = classes()
                .that().haveSimpleNameEndingWith("Mapper")
                .should().resideInAnyPackage("..adapter..")
                .because("Mappers should be in adapter layer for external format conversion");

        controllersShouldBeInAdapterIn.check(classes);
        repositoriesShouldBeInAdapterOut.check(classes);
        mappersShouldBeInAdapterLayer.check(classes);
    }

    @Test
    void testApplicationLayerResponsibilities() {
        // Application services should be Spring components
        ArchRule applicationServicesShouldBeSpringComponents = classes()
                .that().resideInAPackage("..application.service..")
                .and().haveSimpleNameEndingWith("Service")
                .should().beAnnotatedWith("org.springframework.stereotype.Service")
                .because("Application services should be Spring components");

        // Application services should not directly access adapters
        ArchRule applicationServicesShouldNotAccessAdaptersDirectly = noClasses()
                .that().resideInAPackage("..application.service..")
                .should().dependOnClassesThat().resideInAPackage("..adapter..")
                .because("Application services should only depend on ports, not adapters directly");

        applicationServicesShouldBeSpringComponents.check(classes);
        applicationServicesShouldNotAccessAdaptersDirectly.check(classes);
    }

    @Test
    void testCommonsLayerIndependence() {
        // Commons should not depend on business logic
        ArchRule commonsShouldNotDependOnBusinessLogic = noClasses()
                .that().resideInAPackage("..commons..")
                .should().dependOnClassesThat().resideInAPackage("..domain..")
                .because("Commons should be independent of business logic");

        // Commons should not depend on ports
        ArchRule commonsShouldNotDependOnPorts = noClasses()
                .that().resideInAPackage("..commons..")
                .should().dependOnClassesThat().resideInAPackage("..port..")
                .because("Commons should be independent of ports");

        commonsShouldNotDependOnBusinessLogic.check(classes);
        commonsShouldNotDependOnPorts.check(classes);
    }

    @Test
    void testAdapterLayerResponsibilities() {
        // Adapters should be Spring components
        ArchRule adaptersShouldBeSpringComponents = classes()
                .that().resideInAPackage("..adapter..")
                .and().haveSimpleNameEndingWith("Adapter")
                .should().beAnnotatedWith("org.springframework.stereotype.Component")
                .because("Adapters should be Spring components for dependency injection");

        // Input adapters should only depend on input ports
        ArchRule inputAdaptersShouldOnlyDependOnInputPorts = classes()
                .that().resideInAPackage("..adapter.in..")
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage("..port.in..", "..commons..", "java..", "org.springframework..")
                .because("Input adapters should only depend on input ports and framework classes");

        // Output adapters should only depend on output ports and domain
        ArchRule outputAdaptersShouldOnlyDependOnOutputPortsAndDomain = classes()
                .that().resideInAPackage("..adapter.out..")
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage("..port.out..", "..domain..", "..commons..", "java..", "org.springframework..")
                .because("Output adapters should only depend on output ports, domain, and framework classes");

        adaptersShouldBeSpringComponents.check(classes);
        inputAdaptersShouldOnlyDependOnInputPorts.check(classes);
        outputAdaptersShouldOnlyDependOnOutputPortsAndDomain.check(classes);
    }

    @Test
    void testNoCyclicDependencies() {
        // Test for package-level cycles
        ArchRule noPackageCycles = com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices()
                .matching("com.example.(*)..")
                .should().beFreeOfCycles()
                .because("The architecture should not have cyclic dependencies");

        noPackageCycles.check(classes);
    }

    @Test
    void testConfigurationClassesLocation() {
        // Configuration classes should be in application layer
        ArchRule configClassesShouldBeInApplicationLayer = classes()
                .that().areAnnotatedWith("org.springframework.context.annotation.Configuration")
                .should().resideInAPackage("..application.config..")
                .because("Configuration classes should be in application layer");

        configClassesShouldBeInApplicationLayer.check(classes);
    }

    @Test
    void testExceptionHandlingLocation() {
        // Exception handlers should be in adapter or application layer
        ArchRule exceptionHandlersShouldBeInCorrectLayer = classes()
                .that().haveSimpleNameContaining("ErrorHandler")
                .should().resideInAnyPackage("..adapter..", "..application..")
                .because("Exception handling should be in adapter or application layer");

        exceptionHandlersShouldBeInCorrectLayer.check(classes);
    }
} 