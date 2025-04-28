package com.offisync360.account.service;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.offisync360.account.exception.LocalizedException;
import com.offisync360.account.exception.SubscriptionLimitExceededException;
import com.offisync360.account.model.SubscriptionPlan;
import com.offisync360.account.model.Tenant;
import com.offisync360.account.repository.SubscriptionPlanRepository;
import com.offisync360.account.repository.TenantRepository;
import com.offisync360.account.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class SubscriptionEnforcementService {
    private final TenantRepository tenantRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final UserRepository userRepository;

    @Transactional
    public void checkUserLimit(String tenantId) {
        Tenant tenant = getTenantWithPlan(tenantId);
        long currentUserCount = userRepository.countByTenantId(tenant.getTenantId());
        
        if (currentUserCount >= tenant.getSubscriptionPlan().getMaxUsers()) {
            Optional<SubscriptionPlan> nextPlan = subscriptionPlanRepository
                .findFirstByMaxUsersGreaterThanEqualOrderByMonthlyPriceAsc(currentUserCount);
            
            String message = String.format(
                "Maximum users (%d) reached for %s plan. %s",
                tenant.getSubscriptionPlan().getMaxUsers(),
                tenant.getSubscriptionPlan().getName(),
                nextPlan.map(p -> "Upgrade to " + p.getName() + " for " + p.getMaxUsers() + " users.")
                       .orElse("No higher plans available.")
            );
            
            throw new SubscriptionLimitExceededException(message);
        }
    }

    @Transactional
    public boolean upgradeIfNeeded(String tenantId) {
        Tenant tenant = getTenantWithPlan(tenantId);
        long currentUserCount = userRepository.countByTenantId(tenant.getTenantId());
        
        return subscriptionPlanRepository
            .findFirstByMaxUsersGreaterThanEqualOrderByMonthlyPriceAsc(currentUserCount)
            .filter(newPlan -> !newPlan.getId().equals(tenant.getSubscriptionPlan().getId()))
            .map(newPlan -> {
                tenant.setSubscriptionPlan(newPlan);
                tenantRepository.save(tenant);
                return true;
            })
            .orElse(false);
    }

    private Tenant getTenantWithPlan(String uuid) {
        return tenantRepository.findByUuid(uuid)
            .orElseThrow(() -> new LocalizedException("error.tenant.invalid", HttpStatus.BAD_REQUEST));
    }
}
