package com.example.adapter.out.persistence;

import com.example.domain.model.ProcessingError;
import com.example.port.out.PersistErrorPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter for storing processing errors using JPA.
 * <p>
 * Implements the PersistErrorPort to persist ProcessingError domain objects as ErrorEntity JPA entities.
 * Handles conversion, transaction management, and repository interaction for error persistence.
 * <p>
 * <b>Responsibilities:</b>
 * <ul>
 *   <li>Converts ProcessingError domain objects to ErrorEntity JPA entities</li>
 *   <li>Persists errors using the ErrorRepository</li>
 *   <li>Handles transaction boundaries for error persistence</li>
 * </ul>
 * <b>Usage:</b> Used by application and domain layers to persist error information for diagnostics and auditing.
 */
@Component
@RequiredArgsConstructor
public class JpaErrorPersistenceAdapter implements PersistErrorPort {

    private final ErrorRepository errorRepository;
    private final ErrorMapper errorMapper;

    @Override
    @Transactional
    public ProcessingError persistError(ProcessingError error) {
        ErrorEntity entity = errorMapper.toEntity(error);
        ErrorEntity savedEntity = errorRepository.save(entity);
        return errorMapper.toDomain(savedEntity);
    }
} 