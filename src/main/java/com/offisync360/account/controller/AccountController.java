package com.offisync360.account.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.offisync360.account.dto.TenantSignupRequest;
import com.offisync360.account.dto.TenantSignupResponse;
import com.offisync360.account.service.TenantSignupService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final TenantSignupService tenantSignupService;

    @PostMapping("/signup")
    @PreAuthorize("hasRole('account-admin')")
    public ResponseEntity<TenantSignupResponse> signupTenant(
            @Valid @RequestBody TenantSignupRequest request) {
        TenantSignupResponse response = tenantSignupService.signupTenant(request);
        return ResponseEntity.ok(response);
    }
}