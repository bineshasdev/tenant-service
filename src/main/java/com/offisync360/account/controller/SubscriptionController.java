package com.offisync360.account.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.offisync360.account.annotation.RateLimited;
import com.offisync360.account.dto.SubscriptionChangeRequest;
import com.offisync360.account.model.Subscription;
import com.offisync360.account.model.SubscriptionPlan;
import com.offisync360.account.service.SubscriptionManagementService;
import com.offisync360.account.service.SubscriptionPlanService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {
    
    private final SubscriptionPlanService planService;
    private final SubscriptionManagementService subscriptionManagementService;

    // Admin endpoints for subscription plans
    @GetMapping("/plans")
    public List<SubscriptionPlan> getAllPlans() {
        return planService.getAllPlans();
    }

    @PostMapping("/plans")
    public SubscriptionPlan createPlan(@RequestBody SubscriptionPlan plan) {
        return planService.createPlan(plan);
    }

    @PutMapping("/plans/{id}")
    public ResponseEntity<SubscriptionPlan> updatePlan(
            @PathVariable String id,
            @RequestBody SubscriptionPlan plan) {
        return ResponseEntity.of(planService.updatePlan(id, plan));
    }

    // Tenant subscription management endpoints
   
    @PostMapping("/{tenantId}/create")
    public ResponseEntity<Subscription> createSubscription(
            @PathVariable String tenantId,
            @RequestParam String planCode,
            @RequestParam String billingCycle,
            @RequestParam(defaultValue = "false") boolean startTrial,
            HttpServletRequest httpRequest) {
        
        log.info("Creating subscription for tenant {}: {} plan", tenantId, planCode);
        
        Subscription.BillingCycle cycle = Subscription.BillingCycle.valueOf(billingCycle.toUpperCase());
        Subscription subscription = subscriptionManagementService.createSubscription(
                tenantId, planCode, cycle, startTrial, httpRequest);
        
        return ResponseEntity.ok(subscription);
    }

   
    @PutMapping("/{tenantId}/change")
    public ResponseEntity<Subscription> changeSubscription(
            @PathVariable String tenantId,
            @Valid @RequestBody SubscriptionChangeRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("Changing subscription for tenant {}: {} -> {}", 
                tenantId, request.getCurrentPlan(), request.getNewPlan());
        
        Subscription subscription = subscriptionManagementService.changeSubscription(
                tenantId, request.getNewPlan(), request.getBillingCycle(), httpRequest);
        
        return ResponseEntity.ok(subscription);
    }

  
    @PostMapping("/{tenantId}/cancel")
    public ResponseEntity<String> cancelSubscription(
            @PathVariable String tenantId,
            @RequestParam String reason,
            HttpServletRequest httpRequest) {
        
        log.info("Cancelling subscription for tenant {}: {}", tenantId, reason);
        
        subscriptionManagementService.cancelSubscription(tenantId, reason, httpRequest);
        return ResponseEntity.ok("Subscription cancelled successfully");
    }

    @GetMapping("/{tenantId}")
    public ResponseEntity<Subscription> getActiveSubscription(@PathVariable String tenantId) {
        return subscriptionManagementService.getActiveSubscription(tenantId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{tenantId}/usage")
    public ResponseEntity<Map<String, Object>> getSubscriptionUsage(@PathVariable String tenantId) {
        Map<String, Object> usage = subscriptionManagementService.getSubscriptionUsage(tenantId);
        return ResponseEntity.ok(usage);
    }

    // Admin endpoints for subscription management
    @PostMapping("/admin/process-renewals")
    public ResponseEntity<String> processRenewals() {
        subscriptionManagementService.processRenewals();
        return ResponseEntity.ok("Renewals processed successfully");
    }

    @PostMapping("/admin/process-trial-expirations")
    public ResponseEntity<String> processTrialExpirations() {
        subscriptionManagementService.processTrialExpirations();
        return ResponseEntity.ok("Trial expirations processed successfully");
    }
}