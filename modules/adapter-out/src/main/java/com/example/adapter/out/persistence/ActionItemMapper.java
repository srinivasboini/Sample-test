package com.example.adapter.out.persistence;

import com.example.domain.model.ActionItem;
import org.springframework.stereotype.Component;

@Component
public class ActionItemMapper {

    public ActionItemJpaEntity toJpaEntity(ActionItem domain) {
        return ActionItemJpaEntity.builder()
                .id(domain.getId())
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
