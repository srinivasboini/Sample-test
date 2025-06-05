package com.example.application.aspect;

import com.example.commons.annotation.Run;
import com.example.port.out.RunPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * AOP Aspect to handle @Run annotation
 * This aspect intercepts method calls annotated with @Run and triggers the RunPort
 * Located in application module to properly depend on both commons and port-out
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class RunAspect {

    private final RunPort runPort;

    /**
     * Advice that runs after methods annotated with @Run
     * @param joinPoint the join point representing the method execution
     * @param runAnnotation the @Run annotation instance
     */
    @After("@annotation(runAnnotation)")
    public void handleRunAnnotation(JoinPoint joinPoint, Run runAnnotation) {
        try {
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = joinPoint.getSignature().getName();
            String message = runAnnotation.message();
            
            log.debug("Processing @Run annotation on {}.{}", className, methodName);
            
            // Call the port to execute the run operation
            runPort.executeRun(message, methodName, className);
            
        } catch (Exception e) {
            log.error("Error processing @Run annotation", e);
            // Don't rethrow to avoid breaking the original method execution
        }
    }
} 