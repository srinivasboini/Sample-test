package com.example.adapter.out.persistence;

import com.example.domain.model.ActionItem;
import com.example.domain.model.ActionItemStatus;
import com.example.domain.model.TypeCodeCount;
import com.example.port.out.SaveActionItemPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Persistence adapter for Action Items that implements the SaveActionItemPort.
 * 
 * This adapter handles the business logic for action item persistence:
 * - Ensures only one record per uniqueId exists at any time
 * - Handles opening and closing of action items
 * - Maintains proper audit timestamps
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ActionItemPersistenceAdapter implements SaveActionItemPort {

    private final ActionItemRepository actionItemRepository;
    private final ActionItemMapper actionItemMapper;

    @Override
    public ActionItem saveActionItem(ActionItem actionItem) {
        log.debug("Saving action item with uniqueId: {}", actionItem.getUniqueId());
        
        // Check if an action item with this uniqueId already exists
        Optional<ActionItemJpaEntity> existingEntity = actionItemRepository.findByUniqueId(actionItem.getUniqueId());
        
        if (existingEntity.isPresent()) {
            // Update existing record
            ActionItemJpaEntity existing = existingEntity.get();
            log.debug("Found existing action item with uniqueId: {}, updating", actionItem.getUniqueId());
            
            // Update the existing entity with new values
            existing.setTitle(actionItem.getTitle());
            existing.setDescription(actionItem.getDescription());
            existing.setAssignee(actionItem.getAssignee());
            existing.setCategory(actionItem.getCategory());
            existing.setTypeCode(actionItem.getTypeCode());
            existing.setStatus(actionItem.getStatus());
            existing.setDueDate(actionItem.getDueDate());
            existing.setUpdatedAt(LocalDateTime.now());
            
            ActionItemJpaEntity savedEntity = actionItemRepository.save(existing);
            return actionItemMapper.toDomainEntity(savedEntity);
        } else {
            // Create new record
            log.debug("No existing action item found with uniqueId: {}, creating new", actionItem.getUniqueId());
            ActionItemJpaEntity entity = actionItemMapper.toJpaEntity(actionItem);
            ActionItemJpaEntity savedEntity = actionItemRepository.save(entity);
            return actionItemMapper.toDomainEntity(savedEntity);
        }
    }

    @Override
    public Optional<ActionItem> findByUniqueId(String uniqueId) {
        log.debug("Finding action item by uniqueId: {}", uniqueId);
        return actionItemRepository.findByUniqueId(uniqueId)
                .map(actionItemMapper::toDomainEntity);
    }

    @Override
    public List<TypeCodeCount> getTypeCodesByCountAndStatus(ActionItemStatus status) {
        log.debug("Getting typeCodes by count for status: {}", status);
        
        // Get all typeCodes for the given status
        List<String> typeCodes = actionItemRepository.findTypeCodesByStatus(status);
        
        // Count occurrences of each typeCode
        Map<String, Long> typeCodeCounts = typeCodes.stream()
                .collect(Collectors.groupingBy(
                        typeCode -> typeCode,
                        Collectors.counting()
                ));
        
        // Convert to TypeCodeCount objects and sort by count descending
        return typeCodeCounts.entrySet().stream()
                .map(entry -> TypeCodeCount.builder()
                        .typeCode(entry.getKey())
                        .count(entry.getValue())
                        .build())
                .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
                .collect(Collectors.toList());
    }
}
