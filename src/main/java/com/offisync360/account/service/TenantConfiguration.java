package com.offisync360.account.service;
  
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.offisync360.account.model.TenantConfig;
import com.offisync360.account.repository.TenantRepository;
 
import java.util.Optional;
import java.util.UUID;

@Service
public class TenantConfiguration {
 
    private final TenantRepository repository;  
    
    public TenantConfiguration(TenantRepository repository) {
        this.repository = repository;
    }

        @Cacheable(value = "tenantConfigs", key = "#tenant")
        public Optional<TenantConfig> getConfig(String tenant) {
            return repository.findConfigById(UUID.fromString(tenant));
    }

}