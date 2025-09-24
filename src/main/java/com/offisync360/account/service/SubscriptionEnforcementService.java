package com.offisync360.account.service;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.offisync360.account.exception.LocalizedException;
import com.offisync360.account.exception.SubscriptionLimitExceededException;
import com.offisync360.account.model.Subscription;
import com.offisync360.account.model.SubscriptionPlan;
import com.offisync360.account.model.Tenant;
import com.offisync360.account.repository.SubscriptionPlanRepository;
import com.offisync360.account.repository.SubscriptionRepository;
import com.offisync360.account.service.auth.AuthenticationProvider;
import com.offisync360.account.service.auth.AuthenticationProviderFactory;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class SubscriptionEnforcementService {
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final AuthenticationProviderFactory authProviderFactory;

    @Transactional
    public void checkUserLimit(String tenantId) {
        Subscription subscription = getActiveSubscription(tenantId);
        Tenant tenant = subscription.getTenant();
        AuthenticationProvider authProvider = authProviderFactory.getDefaultProvider();
        long currentUserCount = authProvider.getUserCount(tenant.getRealmName());
        
        if (currentUserCount >= subscription.getPlan().getMaxUsers()) {
            Optional<SubscriptionPlan> nextPlan = subscriptionPlanRepository
                .findFirstByMaxUsersGreaterThanEqualOrderByMonthlyPriceAsc(currentUserCount);
            
            String message = String.format(
                "Maximum users (%d) reached for %s plan. %s",
                subscription.getPlan().getMaxUsers(),
                subscription.getPlan().getName(),
                nextPlan.map(p -> "Upgrade to " + p.getName() + " for " + p.getMaxUsers() + " users.")
                       .orElse("No higher plans available.")
            );
            
            throw new SubscriptionLimitExceededException(message);
        }
    }

    @Transactional
    public boolean upgradeIfNeeded(String tenantId) {
        Subscription subscription = getActiveSubscription(tenantId);
        Tenant tenant = subscription.getTenant();
        AuthenticationProvider authProvider = authProviderFactory.getDefaultProvider();
        long currentUserCount = authProvider.getUserCount(tenant.getRealmName());
        
        return subscriptionPlanRepository
            .findFirstByMaxUsersGreaterThanEqualOrderByMonthlyPriceAsc(currentUserCount)
            .filter(newPlan -> !newPlan.getId().equals(subscription.getPlan().getId()))
            .map(newPlan -> {
                subscription.setPlan(newPlan);
                subscriptionRepository.save(subscription);
                return true;
            })
            .orElse(false);
    }

    private Subscription getActiveSubscription(String tenantId) {
        return subscriptionRepository.findActiveSubscriptionByTenantId(tenantId)
            .orElseThrow(() -> new LocalizedException("error.tenant.no_active_subscription", HttpStatus.BAD_REQUEST));
    }
}
