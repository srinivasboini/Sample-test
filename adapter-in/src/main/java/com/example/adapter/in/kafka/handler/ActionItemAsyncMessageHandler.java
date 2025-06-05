package com.example.adapter.in.kafka.handler;

import static java.util.concurrent.CompletableFuture.runAsync;

import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.example.adapter.in.kafka.ActionItemAsyncRequest;

import lombok.extern.slf4j.Slf4j;

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
        log.info("Handling : {}", actionItemAsyncRequest);
        runAsync(() -> messageProcessor.process(actionItemAsyncRequest), messageProcessingExecutor)
                .whenComplete((result, error) -> resultHandler.handleResult(actionItemAsyncRequest, error));

    }

}
