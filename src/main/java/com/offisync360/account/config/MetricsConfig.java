package com.offisync360.account.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    /*@Bean
    MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags(
            "application", "account-service",
            "region", System.getenv().getOrDefault("REGION", "unknown")
        );
    }*/
}