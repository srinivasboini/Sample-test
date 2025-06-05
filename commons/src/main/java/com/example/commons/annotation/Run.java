package com.example.commons.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to mark methods that should trigger port execution
 * This annotation will be processed by an AOP aspect to call the RunPort
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Run {
    /**
     * Optional message to be logged when the annotation is triggered
     * @return the message string
     */
    String message() default "Run annotation triggered";
} 