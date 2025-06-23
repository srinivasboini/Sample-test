package com.example.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model for representing typeCode count results.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypeCodeCount {
    
    /** The type code of the action item */
    private String typeCode;
    
    /** The count of action items with this type code */
    private Long count;
} 