package com.offisync360.account.controller;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.offisync360.account.annotation.RateLimited;
import com.offisync360.account.dto.UserRegistrationRequest;
import com.offisync360.account.model.Subscription;
import com.offisync360.account.model.UsageMetrics;
import com.offisync360.account.model.User;
import com.offisync360.account.service.SubscriptionManagementService;
import com.offisync360.account.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final SubscriptionManagementService subscriptionManagementService;

    /**
     * Creates a new user for a tenant
     */
    @RateLimited
    @PostMapping("/{tenantId}")
    public ResponseEntity<User> createUser(
            @PathVariable String tenantId,
            @Valid @RequestBody UserRegistrationRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("Creating user for tenant: {}", tenantId);
        
        User user = userService.createUser(request, tenantId, httpRequest);
        return ResponseEntity.ok(user);
    }

    /**
     * Gets all users for a tenant
     */
    @GetMapping("/{tenantId}")
    public ResponseEntity<List<User>> getUsersByTenant(@PathVariable String tenantId) {
        List<User> users = userService.getUsersByTenant(tenantId);
        return ResponseEntity.ok(users);
    }

    /**
     * Gets paginated users for a tenant
     */
    @GetMapping("/{tenantId}/page")
    public ResponseEntity<Page<User>> getUsersByTenant(
            @PathVariable String tenantId,
            Pageable pageable) {
        Page<User> users = userService.getUsersByTenant(tenantId, pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Gets user by ID
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable String userId) {
        return userService.getUserById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Updates user information
     */
    @PutMapping("/user/{userId}")
    public ResponseEntity<User> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UserRegistrationRequest request,
            HttpServletRequest httpRequest) {
        
        User user = userService.updateUser(userId, request, httpRequest);
        return ResponseEntity.ok(user);
    }

    /**
     * Deactivates a user
     */
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<String> deactivateUser(
            @PathVariable String userId,
            @RequestParam(required = false) String reason,
            HttpServletRequest httpRequest) {
        
        userService.deactivateUser(userId, reason != null ? reason : "No reason provided", httpRequest);
        return ResponseEntity.ok("User deactivated successfully");
    }

    /**
     * Gets subscription usage for a tenant
     */
    @GetMapping("/{tenantId}/subscription/usage")
    public ResponseEntity<Map<String, Object>> getSubscriptionUsage(@PathVariable String tenantId) {
        Map<String, Object> usage = subscriptionManagementService.getSubscriptionUsage(tenantId);
        return ResponseEntity.ok(usage);
    }

    /**
     * Gets current user usage metrics
     */
    @GetMapping("/{tenantId}/usage")
    public ResponseEntity<UsageMetrics> getCurrentUserUsage(@PathVariable String tenantId) {
        UsageMetrics usage = userService.getCurrentUserUsage(tenantId);
        return ResponseEntity.ok(usage);
    }

    /**
     * Gets active subscription for a tenant
     */
    @GetMapping("/{tenantId}/subscription")
    public ResponseEntity<Subscription> getActiveSubscription(@PathVariable String tenantId) {
        return subscriptionManagementService.getActiveSubscription(tenantId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}