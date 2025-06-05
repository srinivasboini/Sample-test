package com.example.adapter.out.persistence;

import com.example.domain.model.MasterConfiguration;
import com.example.port.out.MasterConfigurationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class MasterConfigurationPersistenceAdapter implements MasterConfigurationPort {
    
    private final MasterConfigurationRepository repository;
    private final MasterConfigurationMapper mapper;
    
    @Override
    public MasterConfiguration save(MasterConfiguration masterConfiguration) {
        log.debug("Saving master configuration: {}", masterConfiguration);
        MasterConfigurationJpaEntity entity = mapper.toEntity(masterConfiguration);
        MasterConfigurationJpaEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }
    
    @Override
    public List<MasterConfiguration> saveAll(List<MasterConfiguration> masterConfigurations) {
        log.debug("Saving {} master configurations", masterConfigurations.size());
        List<MasterConfigurationJpaEntity> entities = mapper.toEntityList(masterConfigurations);
        List<MasterConfigurationJpaEntity> savedEntities = repository.saveAll(entities);
        return mapper.toDomainList(savedEntities);
    }
    
    @Override
    public List<MasterConfiguration> findAllActive() {
        log.debug("Finding all active master configurations");
        List<MasterConfigurationJpaEntity> entities = repository.findByActiveTrue();
        return mapper.toDomainList(entities);
    }
    
    @Override
    public Optional<MasterConfiguration> findByCategoryAndTypeCode(String category, String typeCode) {
        log.debug("Finding master configuration by category: {} and typeCode: {}", category, typeCode);
        Optional<MasterConfigurationJpaEntity> entity = repository.findByCategoryAndTypeCode(category, typeCode);
        return entity.map(mapper::toDomain);
    }
    
    @Override
    public boolean existsByCategoryAndTypeCodeAndActive(String category, String typeCode, boolean active) {
        log.debug("Checking existence of category: {} and typeCode: {} with active: {}", category, typeCode, active);
        return repository.existsByCategoryAndTypeCodeAndActive(category, typeCode, active);
    }
} 