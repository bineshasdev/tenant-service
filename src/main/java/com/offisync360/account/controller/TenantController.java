package com.offisync360.account.controller;

import org.springframework.http.ResponseEntity; 
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable; 
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
 
import com.offisync360.account.dto.SubscriptionUpdateRequest;
import com.offisync360.account.model.Tenant;
import com.offisync360.account.service.TenantService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class TenantController {
    private final TenantService tenantService;
 
    @PatchMapping("/{tenantId}/subscription") 
    public ResponseEntity<Tenant> updateSubscription(
            @PathVariable String tenantId,
            @RequestBody SubscriptionUpdateRequest request) {
        Tenant tenant = tenantService.updateSubscription(tenantId, request.getNewPlan());
        return ResponseEntity.ok(tenant);
    }
}