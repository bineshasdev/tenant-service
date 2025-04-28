package com.offisync360.account.service;
 
import org.springframework.stereotype.Service;
 
import com.offisync360.account.model.SubscriptionPlan;
import com.offisync360.account.model.Tenant; 
import com.offisync360.account.repository.TenantRepository;
 
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TenantService {
    private final TenantRepository tenantRepository; 
    
    public Tenant updateSubscription(String tenantId, SubscriptionPlan newPlan) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        
        tenant.setSubscriptionPlan(newPlan);
        return tenantRepository.save(tenant);
    }
}