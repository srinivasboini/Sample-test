package com.example.application.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = {
    "com.example.domain",
    "com.example.adapter.in",
    "com.example.adapter.out",
    "com.example.application",
    "com.example.commons", "com.example.commons.async"
})
@EntityScan("com.example.adapter.out.persistence")
@EnableJpaRepositories("com.example.adapter.out.persistence")
public class ApplicationConfig {
}
