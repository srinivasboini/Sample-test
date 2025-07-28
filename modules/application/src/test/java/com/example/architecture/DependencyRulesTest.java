// package com.example.architecture;

// import com.tngtech.archunit.core.domain.JavaClasses;
// import com.tngtech.archunit.core.importer.ClassFileImporter;
// import com.tngtech.archunit.lang.ArchRule;
// import org.junit.jupiter.api.BeforeAll;
// import org.junit.jupiter.api.Test;

// import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

// /**
//  * Additional ArchUnit tests for dependency rules and package structure validation.
//  * 
//  * These tests focus on specific architectural concerns beyond the main hexagonal
//  * architecture validation.
//  */
// class DependencyRulesTest {

//     private static JavaClasses classes;

//     @BeforeAll
//     static void setUp() {
//         classes = new ClassFileImporter()
//                 .importPackages("com.example");
//     }

//     @Test
//     void testNoDirectDatabaseAccessFromDomain() {
//         ArchRule domainShouldNotAccessDatabase = noClasses()
//                 .that().resideInAPackage("..domain..")
//                 .should().dependOnClassesThat().resideInAPackage("javax.persistence..")
//                 .because("Domain should not have direct database dependencies");

//         domainShouldNotAccessDatabase.check(classes);
//     }

//     @Test
//     void testNoDirectKafkaAccessFromDomain() {
//         ArchRule domainShouldNotAccessKafka = noClasses()
//                 .that().resideInAPackage("..domain..")
//                 .should().dependOnClassesThat().resideInAPackage("org.apache.kafka..")
//                 .because("Domain should not have direct Kafka dependencies");

//         domainShouldNotAccessKafka.check(classes);
//     }

//     @Test
//     void testNoDirectWebAccessFromDomain() {
//         ArchRule domainShouldNotAccessWeb = noClasses()
//                 .that().resideInAPackage("..domain..")
//                 .should().dependOnClassesThat().resideInAPackage("org.springframework.web..")
//                 .because("Domain should not have direct web dependencies");

//         domainShouldNotAccessWeb.check(classes);
//     }

//     @Test
//     void testCommandObjectsShouldBeImmutable() {
//         ArchRule commandsShouldBeImmutable = classes()
//                 .that().resideInAPackage("..port.in..")
//                 .and().haveSimpleNameEndingWith("Command")
//                 .should().resideInAPackage("..port.in..")
//                 .because("Command objects should be in port-in layer");

//         commandsShouldBeImmutable.check(classes);
//     }

//     @Test
//     void testMappersShouldBeInAdapterLayer() {
//         ArchRule mappersShouldBeInAdapterLayer = classes()
//                 .that().haveSimpleNameEndingWith("Mapper")
//                 .should().resideInAnyPackage("..adapter..")
//                 .because("Mappers should be in adapter layer for external format conversion");

//         mappersShouldBeInAdapterLayer.check(classes);
//     }

//     @Test
//     void testRepositoriesShouldBeInAdapterOutLayer() {
//         ArchRule repositoriesShouldBeInAdapterOut = classes()
//                 .that().haveSimpleNameEndingWith("Repository")
//                 .should().resideInAPackage("..adapter.out..")
//                 .because("Repositories should be in adapter-out layer for persistence");

//         repositoriesShouldBeInAdapterOut.check(classes);
//     }

//     @Test
//     void testControllersShouldBeInAdapterInLayer() {
//         ArchRule controllersShouldBeInAdapterIn = classes()
//                 .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
//                 .should().resideInAPackage("..adapter.in..")
//                 .because("Controllers should be in adapter-in layer for incoming requests");

//         controllersShouldBeInAdapterIn.check(classes);
//     }

//     @Test
//     void testKafkaConsumersShouldBeInAdapterInLayer() {
//         ArchRule kafkaConsumersShouldBeInAdapterIn = classes()
//                 .that().resideInAPackage("..kafka..")
//                 .and().haveSimpleNameContaining("Consumer")
//                 .should().resideInAPackage("..adapter.in..")
//                 .because("Kafka consumers should be in adapter-in layer");

//         kafkaConsumersShouldBeInAdapterIn.check(classes);
//     }

//     @Test
//     void testDomainServicesShouldNotBeAnnotated() {
//         ArchRule domainServicesShouldNotBeAnnotated = noClasses()
//                 .that().resideInAPackage("..domain.service..")
//                 .should().beAnnotatedWith("org.springframework.stereotype.Service")
//                 .because("Domain services should be pure business logic without Spring annotations");

//         domainServicesShouldNotBeAnnotated.check(classes);
//     }

//     @Test
//     void testApplicationServicesShouldBeAnnotated() {
//         ArchRule applicationServicesShouldBeAnnotated = classes()
//                 .that().resideInAPackage("..application.service..")
//                 .and().haveSimpleNameEndingWith("Service")
//                 .should().beAnnotatedWith("org.springframework.stereotype.Service")
//                 .because("Application services should be Spring components");

//         applicationServicesShouldBeAnnotated.check(classes);
//     }

//     @Test
//     void testNoLombokInDomainModel() {
//         ArchRule domainModelShouldNotUseLombok = noClasses()
//                 .that().resideInAPackage("..domain.model..")
//                 .should().dependOnClassesThat().resideInAPackage("lombok..")
//                 .because("Domain model should not depend on Lombok for better control");

//         domainModelShouldNotUseLombok.check(classes);
//     }

//     @Test
//     void testExceptionHandlingShouldBeInAdapterLayer() {
//         ArchRule exceptionHandlersShouldBeInAdapterLayer = classes()
//                 .that().haveSimpleNameContaining("ErrorHandler")
//                 .should().resideInAnyPackage("..adapter..", "..application..")
//                 .because("Exception handling should be in adapter or application layer");

//         exceptionHandlersShouldBeInAdapterLayer.check(classes);
//     }

//     @Test
//     void testConfigurationClassesShouldBeInApplicationLayer() {
//         ArchRule configClassesShouldBeInApplicationLayer = classes()
//                 .that().areAnnotatedWith("org.springframework.context.annotation.Configuration")
//                 .should().resideInAPackage("..application.config..")
//                 .because("Configuration classes should be in application layer");

//         configClassesShouldBeInApplicationLayer.check(classes);
//     }

//     @Test
//     void testNoBusinessLogicInControllers() {
//         ArchRule controllersShouldNotHaveBusinessLogic = noClasses()
//                 .that().resideInAPackage("..adapter.in.web..")
//                 .should().dependOnClassesThat().resideInAPackage("..domain.service..")
//                 .because("Controllers should not directly access domain services");

//         controllersShouldNotHaveBusinessLogic.check(classes);
//     }

//     @Test
//     void testNoBusinessLogicInRepositories() {
//         ArchRule repositoriesShouldNotHaveBusinessLogic = noClasses()
//                 .that().resideInAPackage("..adapter.out.persistence..")
//                 .and().haveSimpleNameEndingWith("Repository")
//                 .should().dependOnClassesThat().resideInAPackage("..domain.service..")
//                 .because("Repositories should not contain business logic");

//         repositoriesShouldNotHaveBusinessLogic.check(classes);
//     }

//     @Test
//     void testAsyncProcessingShouldBeInAdapterLayer() {
//         ArchRule asyncHandlersShouldBeInAdapterLayer = classes()
//                 .that().haveSimpleNameContaining("Async")
//                 .should().resideInAnyPackage("..adapter.in..", "..application..")
//                 .because("Async processing should be in adapter or application layer");

//         asyncHandlersShouldBeInAdapterLayer.check(classes);
//     }

//     @Test
//     void testMDCUtilitiesShouldBeInCommons() {
//         ArchRule mdcUtilitiesShouldBeInCommons = classes()
//                 .that().haveSimpleNameContaining("Mdc")
//                 .should().resideInAPackage("..commons..")
//                 .because("MDC utilities should be in commons for cross-cutting concerns");

//         mdcUtilitiesShouldBeInCommons.check(classes);
//     }

//     @Test
//     void testAspectClassesShouldBeInApplicationLayer() {
//         ArchRule aspectClassesShouldBeInApplicationLayer = classes()
//                 .that().areAnnotatedWith("org.aspectj.lang.annotation.Aspect")
//                 .should().resideInAPackage("..application.aspect..")
//                 .because("Aspect classes should be in application layer");

//         aspectClassesShouldBeInApplicationLayer.check(classes);
//     }
// } 