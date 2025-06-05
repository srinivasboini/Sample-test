package com.example.adapter.in.web;

import io.micrometer.observation.annotation.Observed;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.validation.annotation.Validated;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/action-items")
@RequiredArgsConstructor
public class ActionItemController {
    // Your controller methods will be automatically traced
}
