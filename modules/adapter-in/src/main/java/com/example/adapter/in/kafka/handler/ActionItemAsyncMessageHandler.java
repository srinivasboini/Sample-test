package com.example.adapter.in.kafka.handler;

import static java.util.concurrent.CompletableFuture.runAsync;

import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.example.adapter.in.kafka.ActionItemAsyncRequest;
import com.example.commons.mdc.MdcUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Asynchronous message handler for Action Item processing with MDC context support.
 * 
 * This handler processes ActionItemAsyncRequest messages asynchronously while
 * preserving MDC context for distributed tracing and correlation.
 * 
 * Features:
 * - Asynchronous processing using CompletableFuture
 * - MDC context preservation across async operations
 * - Correlation ID tracking
 * - Comprehensive error handling
 */
@Component
@Slf4j
public class ActionItemAsyncMessageHandler implements MessageHandler<ActionItemAsyncRequest> {

    private final ActionItemMessageProcessor messageProcessor;
    private final ActionItemProcessingResultHandler resultHandler;
    private final Executor messageProcessingExecutor;

    public ActionItemAsyncMessageHandler(
            ActionItemMessageProcessor messageProcessor,
            ActionItemProcessingResultHandler resultHandler,
            @Qualifier("messageProcessingExecutor") Executor messageProcessingExecutor) {
        this.messageProcessor = messageProcessor;
        this.resultHandler = resultHandler;
        this.messageProcessingExecutor = messageProcessingExecutor;
    }

    @Override
    public void handle(ActionItemAsyncRequest actionItemAsyncRequest) {
        // Capture current MDC context for async processing
        var mdcContext = MdcUtils.getContext();
        
        log.info("Handling async request: {} with correlationId: {}", 
                actionItemAsyncRequest, MdcUtils.getCorrelationId());
        
        // Set component and operation context
        MdcUtils.setComponent("ActionItemAsyncMessageHandler");
        MdcUtils.setOperation("handle");
        
        runAsync(() -> {
            try {
                // Restore MDC context in async thread
                MdcUtils.setContext(mdcContext);
                
                log.debug("Processing message in async thread with correlationId: {}", 
                         MdcUtils.getCorrelationId());
                
                messageProcessor.process(actionItemAsyncRequest);
                
            } catch (Exception e) {
                log.error("Error processing message with correlationId: {}", 
                         MdcUtils.getCorrelationId(), e);
                throw e;
            }
        }, messageProcessingExecutor)
        .whenComplete((result, error) -> {
            try {
                // Restore MDC context for result handling
                MdcUtils.setContext(mdcContext);
                
                log.debug("Handling result for correlationId: {} with error: {}", 
                         MdcUtils.getCorrelationId(), error != null);
                
                resultHandler.handleResult(actionItemAsyncRequest, error);
                
            } catch (Exception e) {
                log.error("Error handling result for correlationId: {}", 
                         MdcUtils.getCorrelationId(), e);
            }
        });
    }
}
