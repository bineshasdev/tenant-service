package com.offisync360.account.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
 
import com.offisync360.account.dto.SubscriptionUpdateRequest;
import com.offisync360.account.dto.TenantSignupRequest;
import com.offisync360.account.dto.TenantSignupResponse;
import com.offisync360.account.model.Tenant;
import com.offisync360.account.service.TenantService; 
 
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class AccountController {
 
    private final TenantService tenantService;

  
    @PostMapping("/account/signup") 
    public ResponseEntity<TenantSignupResponse> signupTenant(
            @Valid @RequestBody TenantSignupRequest request ) {
        
        log.info("Tenant signup request received for company: {}", request.getCompanyName());
        
        TenantSignupResponse response = tenantService.registerTenant(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/account/{tenantId}/subscription")
    public ResponseEntity<String> updateSubscription(
            @PathVariable String tenantId,
            @RequestBody SubscriptionUpdateRequest request) {
         tenantService.updateSubscription(tenantId, request.getNewPlan());
        return ResponseEntity.ok("Subscription updated successfully");
    }

    @GetMapping("/account/resolve-tenant")
    public ResponseEntity<Tenant> resolveTenant(
            @RequestParam("id") String identifier) {
        log.info("Resolving tenant for identifier: {}", identifier);
        Tenant tenant = tenantService.findTenantByUsernameOrEmail(identifier);
        if (tenant != null) {
            return ResponseEntity.ok(tenant);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}