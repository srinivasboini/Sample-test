package com.example.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;
import com.example.domain.model.ActionItemStatus;

public interface ActionItemRepository extends JpaRepository<ActionItemJpaEntity, String> {
    
    /**
     * Find an action item by its business unique identifier.
     * 
     * @param uniqueId the business unique identifier
     * @return Optional containing the action item if found
     */
    Optional<ActionItemJpaEntity> findByUniqueId(String uniqueId);
    
    /**
     * Get list of typeCodes filtered by status.
     * 
     * @param status the ActionItemStatus to filter by
     * @return List of typeCodes (String)
     */
    @Query("SELECT DISTINCT ai.typeCode FROM ActionItemJpaEntity ai WHERE ai.status = :status")
    List<String> findTypeCodesByStatus(@Param("status") ActionItemStatus status);
}
