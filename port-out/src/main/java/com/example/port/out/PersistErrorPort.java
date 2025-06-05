package com.example.port.out;

import com.example.domain.model.ProcessingError;

public interface PersistErrorPort {
    /**
     * Persists a processing error to the error storage
     * @param error the error to persist
     * @return the persisted error with any generated fields
     */
    ProcessingError persistError(ProcessingError error);
} 