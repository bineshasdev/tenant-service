package com.offisync360.account.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.offisync360.account.exception.BusinessValidationException;
import com.offisync360.account.model.Subscription;
import com.offisync360.account.model.SubscriptionPlan;
import com.offisync360.account.model.Tenant;
import com.offisync360.account.model.UsageMetrics;
import com.offisync360.account.repository.SubscriptionPlanRepository;
import com.offisync360.account.repository.SubscriptionRepository;
import com.offisync360.account.repository.TenantRepository;
import com.offisync360.account.repository.UsageMetricsRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionManagementService {
    
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final TenantRepository tenantRepository;
    private final UsageMetricsRepository usageMetricsRepository;
    private final AuditService auditService;
    private final EmailNotificationService emailNotificationService;
    
    /**
     * Creates a new subscription for a tenant
     */
    @Transactional
    public Subscription createSubscription(String tenantId, String planCode, 
                                         Subscription.BillingCycle billingCycle, 
                                         boolean startTrial, HttpServletRequest httpRequest) {
        
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessValidationException("Tenant not found"));
        
        SubscriptionPlan plan = subscriptionPlanRepository.findByCode(planCode)
                .orElseThrow(() -> new BusinessValidationException("Subscription plan not found"));
        
        // Check if tenant already has an active subscription
        Optional<Subscription> existingSubscription = subscriptionRepository
                .findActiveSubscriptionByTenantId(tenantId);
        
        if (existingSubscription.isPresent()) {
            throw new BusinessValidationException("Tenant already has an active subscription");
        }
        
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = calculateEndDate(startDate, billingCycle);
        LocalDateTime nextBillingDate = endDate;
        LocalDateTime trialEndDate = startTrial ? startDate.plusDays(14) : null;
        
        Subscription subscription = Subscription.builder()
                .tenant(tenant)
                .plan(plan)
                .status(startTrial ? Subscription.SubscriptionStatus.TRIAL : Subscription.SubscriptionStatus.ACTIVE)
                .billingCycle(billingCycle)
                .currentPrice(calculatePrice(plan, billingCycle))
                .startDate(startDate)
                .endDate(endDate)
                .nextBillingDate(nextBillingDate)
                .trialEndDate(trialEndDate)
                .autoRenew(true)
                .build();
        
        subscriptionRepository.save(subscription);
        
        // Update tenant's subscription plan
        tenant.setSubscriptionPlan(plan);
        tenantRepository.save(tenant);
        
        // Log audit event
        auditService.logAuditEvent("SUBSCRIPTION", subscription.getId().toString(), "CREATE", 
                null, subscription, getCurrentUserId(httpRequest), httpRequest);
        
        log.info("Subscription created for tenant {}: {} plan", tenantId, planCode);
        return subscription;
    }
    
    /**
     * Upgrades or downgrades a subscription
     */
    @Transactional
    public Subscription changeSubscription(String tenantId, String newPlanCode, 
                                         Subscription.BillingCycle newBillingCycle,
                                         HttpServletRequest httpRequest) {
        
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessValidationException("Tenant not found"));
        
        SubscriptionPlan newPlan = subscriptionPlanRepository.findByCode(newPlanCode)
                .orElseThrow(() -> new BusinessValidationException("Subscription plan not found"));
        
        Subscription currentSubscription = subscriptionRepository
                .findActiveSubscriptionByTenantId(tenantId)
                .orElseThrow(() -> new BusinessValidationException("No active subscription found"));
        
        // Check if it's actually a change
        if (currentSubscription.getPlan().getCode().equals(newPlanCode) && 
            currentSubscription.getBillingCycle() == newBillingCycle) {
            throw new BusinessValidationException("No changes detected");
        }
        
        // Calculate prorated amount (simplified)
        BigDecimal proratedAmount = calculateProratedAmount(currentSubscription, newPlan, newBillingCycle);
        
        // Create new subscription
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = calculateEndDate(startDate, newBillingCycle);
        
        Subscription newSubscription = Subscription.builder()
                .tenant(tenant)
                .plan(newPlan)
                .status(Subscription.SubscriptionStatus.ACTIVE)
                .billingCycle(newBillingCycle)
                .currentPrice(calculatePrice(newPlan, newBillingCycle))
                .startDate(startDate)
                .endDate(endDate)
                .nextBillingDate(endDate)
                .autoRenew(true)
                .build();
        
        // Cancel current subscription
        currentSubscription.setStatus(Subscription.SubscriptionStatus.CANCELLED);
        currentSubscription.setCancelledAt(LocalDateTime.now());
        currentSubscription.setCancellationReason("Upgraded to " + newPlanCode);
        
        subscriptionRepository.save(currentSubscription);
        subscriptionRepository.save(newSubscription);
        
        // Update tenant's subscription plan
        tenant.setSubscriptionPlan(newPlan);
        tenantRepository.save(tenant);
        
        // Log audit event
        auditService.logAuditEvent("SUBSCRIPTION", newSubscription.getId().toString(), "CHANGE", 
                currentSubscription, newSubscription, getCurrentUserId(httpRequest), httpRequest);
        
        log.info("Subscription changed for tenant {}: {} -> {}", tenantId, 
                currentSubscription.getPlan().getCode(), newPlanCode);
        
        return newSubscription;
    }
    
    /**
     * Cancels a subscription
     */
    @Transactional
    public void cancelSubscription(String tenantId, String reason, HttpServletRequest httpRequest) {
        Subscription subscription = subscriptionRepository
                .findActiveSubscriptionByTenantId(tenantId)
                .orElseThrow(() -> new BusinessValidationException("No active subscription found"));
        
        subscription.setStatus(Subscription.SubscriptionStatus.CANCELLED);
        subscription.setCancelledAt(LocalDateTime.now());
        subscription.setCancellationReason(reason);
        subscription.setAutoRenew(false);
        
        subscriptionRepository.save(subscription);
        
        // Log audit event
        auditService.logAuditEvent("SUBSCRIPTION", subscription.getId().toString(), "CANCEL", 
                null, "Reason: " + reason, getCurrentUserId(httpRequest), httpRequest);
        
        log.info("Subscription cancelled for tenant {}: {}", tenantId, reason);
    }
    
    /**
     * Gets subscription details for a tenant
     */
    public Optional<Subscription> getActiveSubscription(String tenantId) {
        return subscriptionRepository.findActiveSubscriptionByTenantId(tenantId);
    }
    
    /**
     * Gets subscription usage and limits
     */
    public Map<String, Object> getSubscriptionUsage(String tenantId) {
        Subscription subscription = subscriptionRepository
                .findActiveSubscriptionByTenantId(tenantId)
                .orElseThrow(() -> new BusinessValidationException("No active subscription found"));
        
        // Get current usage metrics
        List<UsageMetrics> currentMetrics = usageMetricsRepository
                .findByTenantIdAndMetricTypeOrderByMetricDateDesc(tenantId, UsageMetrics.MetricType.USERS.getValue());
        
        UsageMetrics latestUsage = currentMetrics.isEmpty() ? null : currentMetrics.get(0);
        
        return Map.of(
            "subscription", subscription,
            "currentUsage", latestUsage != null ? latestUsage.getCurrentUsage() : 0,
            "maxUsers", subscription.getPlan().getMaxUsers(),
            "usagePercentage", latestUsage != null ? latestUsage.getPercentageUsed() : 0.0,
            "isOverLimit", latestUsage != null ? latestUsage.getIsOverLimit() : false,
            "daysUntilRenewal", subscription.getNextBillingDate() != null ? 
                java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), subscription.getNextBillingDate()) : 0
        );
    }
    
    /**
     * Processes subscription renewals
     */
    @Transactional
    public void processRenewals() {
        LocalDateTime now = LocalDateTime.now();
        List<Subscription> subscriptionsToRenew = subscriptionRepository
                .findByStatusAndNextBillingDateBefore(Subscription.SubscriptionStatus.ACTIVE, now);
        
        for (Subscription subscription : subscriptionsToRenew) {
            if (subscription.getAutoRenew()) {
                renewSubscription(subscription);
            } else {
                // Mark as expired
                subscription.setStatus(Subscription.SubscriptionStatus.EXPIRED);
                subscriptionRepository.save(subscription);
            }
        }
        
        log.info("Processed {} subscription renewals", subscriptionsToRenew.size());
    }
    
    /**
     * Processes trial expirations
     */
    @Transactional
    public void processTrialExpirations() {
        LocalDateTime now = LocalDateTime.now();
        List<Subscription> expiredTrials = subscriptionRepository.findExpiredTrials(now);
        
        for (Subscription subscription : expiredTrials) {
            // Convert trial to active subscription or cancel
            if (subscription.getAutoRenew()) {
                subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
                subscription.setTrialEndDate(null);
                subscriptionRepository.save(subscription);
                
                // Send trial ended notification
                try {
                    emailNotificationService.sendTrialEndedNotification(subscription.getTenant());
                } catch (Exception e) {
                    log.warn("Failed to send trial ended notification: {}", e.getMessage());
                }
            } else {
                subscription.setStatus(Subscription.SubscriptionStatus.CANCELLED);
                subscription.setCancelledAt(now);
                subscription.setCancellationReason("Trial expired");
                subscriptionRepository.save(subscription);
            }
        }
        
        log.info("Processed {} trial expirations", expiredTrials.size());
    }
    
    /**
     * Renews a subscription
     */
    private void renewSubscription(Subscription subscription) {
        LocalDateTime newStartDate = subscription.getNextBillingDate();
        LocalDateTime newEndDate = calculateEndDate(newStartDate, subscription.getBillingCycle());
        
        subscription.setStartDate(newStartDate);
        subscription.setEndDate(newEndDate);
        subscription.setNextBillingDate(newEndDate);
        subscription.setUpdatedAt(LocalDateTime.now());
        
        subscriptionRepository.save(subscription);
        
        log.info("Renewed subscription for tenant {}", subscription.getTenant().getId());
    }
    
    /**
     * Calculates end date based on billing cycle
     */
    private LocalDateTime calculateEndDate(LocalDateTime startDate, Subscription.BillingCycle billingCycle) {
        return switch (billingCycle) {
            case MONTHLY -> startDate.plusMonths(1);
            case QUARTERLY -> startDate.plusMonths(3);
            case YEARLY -> startDate.plusYears(1);
        };
    }
    
    /**
     * Calculates price based on plan and billing cycle
     */
    private BigDecimal calculatePrice(SubscriptionPlan plan, Subscription.BillingCycle billingCycle) {
        BigDecimal basePrice = plan.getMonthlyPrice();
        return switch (billingCycle) {
            case MONTHLY -> basePrice;
            case QUARTERLY -> basePrice.multiply(BigDecimal.valueOf(3)).multiply(BigDecimal.valueOf(0.9)); // 10% discount
            case YEARLY -> basePrice.multiply(BigDecimal.valueOf(12)).multiply(BigDecimal.valueOf(0.8)); // 20% discount
        };
    }
    
    /**
     * Calculates prorated amount for subscription changes
     */
    private BigDecimal calculateProratedAmount(Subscription current, SubscriptionPlan newPlan, 
                                             Subscription.BillingCycle newBillingCycle) {
        // Simplified proration calculation
        // In a real system, this would be more complex
        return calculatePrice(newPlan, newBillingCycle);
    }
    
    /**
     * Gets current user ID from security context
     */
    private String getCurrentUserId(HttpServletRequest request) {
        // TODO: Implement based on your security setup
        return "system";
    }
}