package com.example.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasterConfigurationRepository extends JpaRepository<MasterConfigurationJpaEntity, String> {
    
    /**
     * Find all active master configurations
     */
    List<MasterConfigurationJpaEntity> findByActiveTrue();
    
    /**
     * Find master configuration by category and type code
     */
    Optional<MasterConfigurationJpaEntity> findByCategoryAndTypeCode(String category, String typeCode);
    
    /**
     * Check if a category-type combination exists and is active
     */
    boolean existsByCategoryAndTypeCodeAndActive(String category, String typeCode, Boolean active);
    
    /**
     * Find by category and type code with active status
     */
    @Query("SELECT mc FROM MasterConfigurationJpaEntity mc WHERE mc.category = :category AND mc.typeCode = :typeCode AND mc.active = :active")
    Optional<MasterConfigurationJpaEntity> findByCategoryAndTypeCodeAndActive(
            @Param("category") String category, 
            @Param("typeCode") String typeCode, 
            @Param("active") Boolean active);
} 