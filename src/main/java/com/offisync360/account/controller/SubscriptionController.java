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

import com.offisync360.account.model.SubscriptionPlan;
import com.offisync360.account.model.Tenant;
import com.offisync360.account.repository.TenantRepository;
import com.offisync360.account.service.SubscriptionEnforcementService;
import com.offisync360.account.service.SubscriptionPlanService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {
     private final SubscriptionPlanService planService;
    private final SubscriptionEnforcementService enforcementService;
    private final TenantRepository tenantRepository;

    // Admin endpoints
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
            @PathVariable Long id,
            @RequestBody SubscriptionPlan plan) {
        return ResponseEntity.of(planService.updatePlan(id, plan));
    }

    // Tenant admin endpoints
    @PostMapping("/{tenantId}/upgrade")
    public ResponseEntity<Tenant> upgradePlan(
            @PathVariable String tenantId,
            @RequestParam String code) {
        return tenantRepository.findById(tenantId)
                .flatMap(tenant -> planService.getAllPlans().stream()
                        .filter(p -> p.getCode().equals(code))
                        .findFirst()
                        .map(newPlan -> {
                            tenant.setSubscriptionPlan(newPlan);
                            return ResponseEntity.ok(tenantRepository.save(tenant));
                        }))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{tenantId}/usage")
    public ResponseEntity<Map<String, Object>> getUsage(@PathVariable String tenantId) {
        return tenantRepository.findById(tenantId)
                .map(tenant -> {
                    int userCount = tenant.getUsers().size();
                    SubscriptionPlan plan = tenant.getSubscriptionPlan();
                    return ResponseEntity.ok(Map.of(
                        "currentUsers", userCount,
                        "maxUsers", plan.getMaxUsers(),
                        "remainingUsers", plan.getMaxUsers() - userCount,
                        "plan", plan
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}