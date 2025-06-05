# @Run Annotation Implementation

This document describes the custom `@Run` annotation implementation following hexagonal architecture principles.

## Architecture Overview

The `@Run` annotation implementation follows the hexagonal architecture pattern with clear separation of concerns:

```
┌─────────────────┐    ┌──────────────┐    ┌─────────────────┐
│   @Run          │    │   RunPort    │    │   RunAdapter    │
│   Annotation    │───▶│  (Port-Out)  │───▶│  (Adapter-Out)  │
│   (Commons)     │    │   Interface  │    │ Implementation  │
└─────────────────┘    └──────────────┘    └─────────────────┘
         │                       │
         └───────────────────────┼─────────────────────────────┐
                                 │                             │
                       ┌─────────▼──────────────────────────────▼──┐
                       │          RunAspect                       │
                       │        (Application)                     │
                       │   AOP Aspect connecting                  │
                       │   annotation to port                     │
                       └──────────────────────────────────────────┘
```

## Components

### 1. @Run Annotation (`commons` module)
- **Location**: `commons/src/main/java/com/example/commons/annotation/Run.java`
- **Purpose**: Custom annotation that can be applied to methods
- **Features**:
  - Can be applied to any method (`@Target(ElementType.METHOD)`)
  - Processed at runtime (`@Retention(RetentionPolicy.RUNTIME)`)
  - Optional message parameter with default value

```java
@Run // Uses default message
public void someMethod() { ... }

@Run(message = "Custom operation executed")
public void anotherMethod() { ... }
```

### 2. RunAspect (`application` module)
- **Location**: `application/src/main/java/com/example/application/aspect/RunAspect.java`
- **Purpose**: AOP aspect that intercepts methods annotated with `@Run`
- **Architecture Note**: Located in application module to properly depend on both commons and port-out modules
- **Features**:
  - Uses Spring AOP (`@Aspect`)
  - Executes after method completion (`@After`)
  - Extracts method and class information
  - Calls the RunPort to execute the operation
  - Error handling to prevent breaking original method execution

### 3. RunPort Interface (`port-out` module)
- **Location**: `port-out/src/main/java/com/example/port/out/RunPort.java`
- **Purpose**: Defines the contract for run operations
- **Method**: `executeRun(String message, String methodName, String className)`

### 4. RunAdapter Implementation (`adapter-out` module)
- **Location**: `adapter-out/src/main/java/com/example/adapter/out/RunAdapter.java`
- **Purpose**: Concrete implementation of RunPort
- **Current Implementation**: 
  - Logs information with emojis for better visibility
  - Prints formatted output to console
  - Placeholder for future business logic

## Usage Examples

### Basic Usage
```java
@Service
public class MyService {
    
    @Run
    public void performAction() {
        // Your business logic here
        // The @Run annotation will trigger after this method executes
    }
    
    @Run(message = "Processing important data")
    public void processData() {
        // Your business logic here
    }
}
```

### Testing the Implementation

The implementation includes test endpoints in `RunTestController`:

1. **Default @Run test**: `GET /api/run-test/default`
2. **Custom @Run test**: `GET /api/run-test/custom`
3. **@Run with parameters**: `POST /api/run-test/process?userId=123&data=sample`
4. **Regular method (no @Run)**: `GET /api/run-test/regular`
5. **Test all methods**: `GET /api/run-test/all`

## Dependencies

### Commons Module
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
<!-- Note: No dependency on port-out to maintain clean architecture -->
```

### Application Module
```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>commons</artifactId>
</dependency>
<dependency>
    <groupId>com.example</groupId>
    <artifactId>port-out</artifactId>
</dependency>
<dependency>
    <groupId>com.example</groupId>
    <artifactId>adapter-out</artifactId>
</dependency>
<!-- RunAspect is located here to properly access both commons and port-out -->
```

## Output Example

When a method annotated with `@Run` is executed, you'll see output like:

```
INFO  - 🚀 Run annotation triggered!
INFO  - 📍 Class: ExampleService
INFO  - 🔧 Method: performDefaultAction
INFO  - 💬 Message: Run annotation triggered
INFO  - ⏰ Timestamp: 2024-01-15T10:30:45.123Z
==================================================
RUN ADAPTER EXECUTED
Class: ExampleService
Method: performDefaultAction
Message: Run annotation triggered
==================================================
```

## Architectural Principles

This implementation follows hexagonal architecture principles:

1. **Commons Module**: Contains only the `@Run` annotation definition - no dependencies on business logic
2. **Port-Out Module**: Defines the contract (`RunPort` interface) - no dependencies on implementation details
3. **Adapter-Out Module**: Provides concrete implementation - depends on port-out
4. **Application Module**: Contains the `RunAspect` that bridges the annotation to the port - can depend on both commons and port-out

This ensures proper dependency direction and maintainability.

## Future Enhancements

The current implementation is a foundation that can be extended with:

1. **Business Logic**: Replace the simple print statements in `RunAdapter` with actual business operations
2. **Configuration**: Add configuration options for different run behaviors
3. **Metrics**: Integrate with monitoring systems to track annotation usage
4. **Async Processing**: Support for asynchronous execution of run operations
5. **Conditional Execution**: Add conditions to control when the annotation should trigger

## Technical Notes

- The aspect uses `@After` advice, meaning it executes after the annotated method completes successfully
- Error handling ensures that exceptions in the aspect don't break the original method execution
- The implementation is thread-safe and can handle concurrent method executions
- Spring's dependency injection is used to wire the RunPort implementation
- **Architecture**: The aspect is placed in the application module to maintain clean dependency flow

## Troubleshooting

1. **Annotation not triggering**: Ensure the method is called on a Spring-managed bean (not `new` instances)
2. **RunPort not found**: Verify that component scanning includes the adapter-out package
3. **AOP not working**: Check that `@EnableAspectJAutoProxy` is enabled (usually automatic with Spring Boot)
4. **Dependency issues**: Ensure the application module has dependencies on both commons and port-out modules

## Future Enhancements

The current implementation is a foundation that can be extended with:

1. **Business Logic**: Replace the simple print statements in `RunAdapter` with actual business operations
2. **Configuration**: Add configuration options for different run behaviors
3. **Metrics**: Integrate with monitoring systems to track annotation usage
4. **Async Processing**: Support for asynchronous execution of run operations
5. **Conditional Execution**: Add conditions to control when the annotation should trigger

## Technical Notes

- The aspect uses `@After` advice, meaning it executes after the annotated method completes successfully
- Error handling ensures that exceptions in the aspect don't break the original method execution
- The implementation is thread-safe and can handle concurrent method executions
- Spring's dependency injection is used to wire the RunPort implementation

## Troubleshooting

1. **Annotation not triggering**: Ensure the method is called on a Spring-managed bean (not `new` instances)
2. **RunPort not found**: Verify that component scanning includes the adapter-out package
3. **AOP not working**: Check that `@EnableAspectJAutoProxy` is enabled (usually automatic with Spring Boot) 