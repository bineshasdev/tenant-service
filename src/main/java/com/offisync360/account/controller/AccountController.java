package com.offisync360.account.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.offisync360.account.annotation.RateLimited;
import com.offisync360.account.dto.SubscriptionUpdateRequest;
import com.offisync360.account.dto.TenantSignupRequest;
import com.offisync360.account.dto.TenantSignupResponse;
import com.offisync360.account.model.Tenant;
import com.offisync360.account.service.TenantService;
import com.offisync360.account.service.TenantSignupService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final TenantSignupService tenantSignupService;
    private final TenantService tenantService;

    @RateLimited
    @PostMapping("/signup") 
    public ResponseEntity<TenantSignupResponse> signupTenant(
            @Valid @RequestBody TenantSignupRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("Tenant signup request received for company: {}", request.getCompanyName());
        
        TenantSignupResponse response = tenantSignupService.signupTenant(request, httpRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{tenantId}/subscription")
    public ResponseEntity<Tenant> updateSubscription(
            @PathVariable String tenantId,
            @RequestBody SubscriptionUpdateRequest request) {
        Tenant updatedTenant = tenantService.updateSubscription(tenantId, request.getNewPlan());
        return ResponseEntity.ok(updatedTenant);
    }
}