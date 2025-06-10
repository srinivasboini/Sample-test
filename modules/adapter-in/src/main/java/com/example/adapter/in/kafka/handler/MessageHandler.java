package com.example.adapter.in.kafka.handler;

/**
 * Generic interface for message processing operations.
 */
@FunctionalInterface
public interface MessageHandler<T> {
    void handle(T request);
}