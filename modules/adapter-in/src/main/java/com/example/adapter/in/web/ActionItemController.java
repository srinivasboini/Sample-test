package com.example.adapter.in.web;

import com.example.domain.model.ActionItemStatus;
import com.example.domain.model.TypeCodeCount;
import com.example.port.out.SaveActionItemPort;
import io.micrometer.observation.annotation.Observed;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.annotation.Validated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RestController
@RequestMapping("/api/action-items")
@RequiredArgsConstructor
@Slf4j
public class ActionItemController {
    
    private final SaveActionItemPort saveActionItemPort;
    
    /**
     * Get list of typeCodes with their counts filtered by status.
     * 
     * @param status the ActionItemStatus to filter by (OPEN or CLOSE)
     * @return List of TypeCodeCount containing typeCode and count
     */
    @GetMapping("/type-codes/count")
    @Observed(name = "get.typeCodes.by.count", contextualName = "get-typeCodes-by-count")
    public List<TypeCodeCount> getTypeCodesByCount(@RequestParam ActionItemStatus status) {
        log.info("Getting typeCodes by count for status: {}", status);
        return saveActionItemPort.getTypeCodesByCountAndStatus(status);
    }
}
