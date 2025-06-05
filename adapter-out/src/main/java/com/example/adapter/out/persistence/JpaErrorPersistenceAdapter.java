package com.example.adapter.out.persistence;

import com.example.domain.model.ProcessingError;
import com.example.port.out.PersistErrorPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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