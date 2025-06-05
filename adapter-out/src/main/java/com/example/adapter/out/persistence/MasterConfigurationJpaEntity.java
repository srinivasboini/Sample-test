package com.example.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "master_configuration", 
       indexes = {
           @Index(name = "idx_category_type_code", columnList = "category, type_code"),
           @Index(name = "idx_active", columnList = "active")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_category_type_code", columnNames = {"category", "type_code"})
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class MasterConfigurationJpaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "category", nullable = false, length = 100)
    private String category;
    
    @Column(name = "type_code", nullable = false, length = 100)
    private String typeCode;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
} 