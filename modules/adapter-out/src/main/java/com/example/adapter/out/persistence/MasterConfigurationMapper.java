package com.example.adapter.out.persistence;

import com.example.domain.model.MasterConfiguration;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MasterConfigurationMapper {
    
    /**
     * Convert domain model to JPA entity
     */
    public MasterConfigurationJpaEntity toEntity(MasterConfiguration domain) {
        if (domain == null) {
            return null;
        }
        
        return MasterConfigurationJpaEntity.builder()
                .id(domain.getId())
                .category(domain.getCategory())
                .typeCode(domain.getTypeCode())
                .description(domain.getDescription())
                .active(domain.isActive())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
    
    /**
     * Convert JPA entity to domain model
     */
    public MasterConfiguration toDomain(MasterConfigurationJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return MasterConfiguration.builder()
                .id(entity.getId())
                .category(entity.getCategory())
                .typeCode(entity.getTypeCode())
                .description(entity.getDescription())
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    /**
     * Convert list of JPA entities to list of domain models
     */
    public List<MasterConfiguration> toDomainList(List<MasterConfigurationJpaEntity> entities) {
        if (entities == null) {
            return null;
        }
        
        return entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
    
    /**
     * Convert list of domain models to list of JPA entities
     */
    public List<MasterConfigurationJpaEntity> toEntityList(List<MasterConfiguration> domains) {
        if (domains == null) {
            return null;
        }
        
        return domains.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
} 