package com.example.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

/**
 * Simple ArchUnit tests for hexagonal architecture validation.
 * 
 * These tests focus on the most critical architectural rules for your project.
 */
class SimpleArchitectureTest {

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

        domainShouldNotDependOnAdapters.check(classes);
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

    @Test
    void testMappersShouldBeInAdapterLayer() {
        ArchRule mappersShouldBeInAdapterLayer = classes()
                .that().haveSimpleNameEndingWith("Mapper")
                .should().resideInAnyPackage("..adapter..")
                .because("Mappers should be in adapter layer for external format conversion");

        mappersShouldBeInAdapterLayer.check(classes);
    }

    @Test
    void testNoDirectDatabaseAccessFromDomain() {
        ArchRule domainShouldNotAccessDatabase = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("javax.persistence..")
                .because("Domain should not have direct database dependencies");

        domainShouldNotAccessDatabase.check(classes);
    }

    @Test
    void testNoDirectSpringAccessFromDomain() {
        ArchRule domainShouldNotAccessSpring = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("org.springframework..")
                .because("Domain should not depend on Spring framework");

        domainShouldNotAccessSpring.check(classes);
    }

    @Test
    void testCommonsLayerIndependence() {
        ArchRule commonsShouldNotDependOnBusinessLogic = noClasses()
                .that().resideInAPackage("..commons..")
                .should().dependOnClassesThat().resideInAPackage("..domain..")
                .because("Commons should be independent of business logic");

        commonsShouldNotDependOnBusinessLogic.check(classes);
    }
} 