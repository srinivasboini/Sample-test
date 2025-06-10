package com.example.adapter.out.persistence;

import com.example.domain.model.ActionItem;
import com.example.port.out.SaveActionItemPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ActionItemPersistenceAdapter implements SaveActionItemPort {

    private final ActionItemRepository actionItemRepository;
    private final ActionItemMapper actionItemMapper;

    @Override
    public ActionItem saveActionItem(ActionItem actionItem) {
        ActionItemJpaEntity entity = actionItemMapper.toJpaEntity(actionItem);
        ActionItemJpaEntity savedEntity = actionItemRepository.save(entity);
        return actionItemMapper.toDomainEntity(savedEntity);
    }
}
