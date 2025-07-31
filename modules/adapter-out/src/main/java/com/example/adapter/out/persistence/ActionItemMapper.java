package com.example.adapter.out.persistence;

import com.example.domain.model.ActionItem;
import org.springframework.stereotype.Component;

/**
 * Maps between ActionItem domain models and JPA entities for persistence operations.
 * <p>
 * This component is responsible for converting ActionItem objects between their domain
 * representation and their database (JPA entity) representation. It ensures that all
 * relevant fields are mapped correctly for both persistence and retrieval.
 * <p>
 * <b>Responsibilities:</b>
 * <ul>
 *   <li>Converts ActionItem domain objects to JPA entities for storage</li>
 *   <li>Converts JPA entities to ActionItem domain objects for business logic</li>
 *   <li>Ensures field consistency and handles mapping logic</li>
 * </ul>
 * <b>Usage:</b> Used by persistence adapters and repositories to translate between domain and entity layers.
 */
@Component
public class ActionItemMapper {

    public ActionItemJpaEntity toJpaEntity(ActionItem domain) {
        return ActionItemJpaEntity.builder()
                .id(domain.getId())
                .uniqueId(domain.getUniqueId())
                .title(domain.getTitle())
                .description(domain.getDescription())
                .assignee(domain.getAssignee())
                .category(domain.getCategory())
                .typeCode(domain.getTypeCode())
                .status(domain.getStatus())
                .dueDate(domain.getDueDate())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public ActionItem toDomainEntity(ActionItemJpaEntity entity) {
        return ActionItem.builder()
                .id(entity.getId())
                .uniqueId(entity.getUniqueId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .assignee(entity.getAssignee())
                .category(entity.getCategory())
                .typeCode(entity.getTypeCode())
                .status(entity.getStatus())
                .dueDate(entity.getDueDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
