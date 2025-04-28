package com.offisync360.account.service;

import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class TenantMetricsService {
    
    private final Counter tenantCreationCounter;
    private final DistributionSummary tenantCreationDuration;

    public TenantMetricsService(MeterRegistry registry) {
        this.tenantCreationCounter = Counter.builder("tenant.creation.count")
            .description("Total tenant creations")
            .tag("type", "signup")
            .register(registry);
            
        this.tenantCreationDuration = DistributionSummary.builder("tenant.creation.duration")
            .description("Time taken to create tenant")
            .baseUnit("milliseconds")
            .register(registry);
    }
    
    public void recordTenantCreation(long durationMs) {
        tenantCreationCounter.increment();
        tenantCreationDuration.record(durationMs);
    }
}