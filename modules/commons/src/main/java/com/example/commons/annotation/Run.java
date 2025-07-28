package com.example.commons.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to mark methods that should trigger port execution.
 * <p>
 * This annotation is intended for use with methods that need to be intercepted by an AOP aspect
 * (such as RunAspect) to trigger execution of a port (e.g., RunPort) in a hexagonal architecture.
 * <ul>
 *   <li>Retention: RUNTIME - available at runtime for reflection and AOP processing.</li>
 *   <li>Target: METHOD - can only be applied to methods.</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>
 *     @Run(message = "Triggering port execution")
 *     public void execute() { ... }
 * </pre>
 * <p>
 * The optional message attribute can be used for logging or tracing purposes.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Run {
    /**
     * Optional message to be logged when the annotation is triggered.
     * This can be used by aspects for tracing or debugging.
     *
     * @return the message string
     */
    String message() default "Run annotation triggered";
} 