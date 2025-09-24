package com.offisync360.account.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.offisync360.account.dto.UserRegistrationRequest;
import com.offisync360.account.exception.BusinessValidationException; 
import com.offisync360.account.model.Tenant;
import com.offisync360.account.model.UsageMetrics;
import com.offisync360.account.model.User;
import com.offisync360.account.repository.SubscriptionRepository;
import com.offisync360.account.repository.TenantRepository;
import com.offisync360.account.repository.UsageMetricsRepository;
import com.offisync360.account.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService { 
    
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UsageMetricsRepository usageMetricsRepository;
    private final KeycloakAdminClient keycloakAdmin;
    private final SubscriptionEnforcementService subscriptionService;
    private final AuditService auditService;
    private final EmailNotificationService emailNotificationService;
    private final MobileValidationService mobileValidationService;
    
    /**
     * Creates a new user for a tenant
     */
    @Transactional
    public User createUser(UserRegistrationRequest request, String tenantId, HttpServletRequest httpRequest) {
        // Check subscription limits
        subscriptionService.checkUserLimit(tenantId);
        
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessValidationException("Tenant not found"));
        
        // Check if user already exists
        if (userRepository.findByEmailAndTenantId(request.getEmail(), tenantId).isPresent()) {
            throw new BusinessValidationException("User with this email already exists in the tenant");
        }
        
        // Generate user ID (matches Keycloak user ID)
        String userId = UUID.randomUUID().toString();
        
        // Create user entity
        User user = User.builder()
                .id(UUID.fromString(userId))
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .tenant(tenant)
                .status(User.UserStatus.PENDING_VERIFICATION)
                .role(User.UserRole.USER)
                .isAdmin(false)
                .startDateEffective(LocalDateTime.now())
                .build();
        
        // Save user
        userRepository.save(user);
        
        // Create user in Keycloak
        try {
           /*   keycloakAdmin.createUser(
                    tenant.getRealmName(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getFirstName(),
                    request.getLastName()
            ); */
        } catch (Exception e) {
            log.error("Failed to create user in Keycloak: {}", e.getMessage());
            // Rollback user creation
            userRepository.delete(user);
            throw new BusinessValidationException("Failed to create user: " + e.getMessage());
        }
        
        // Update usage metrics
        updateUserUsageMetrics(tenantId);
        
        // Send welcome email
        try {
            emailNotificationService.sendWelcomeEmail(
                    tenantId, 
                    userId, 
                    request.getEmail(), 
                    user.getFullName()
            );
        } catch (Exception e) {
            log.warn("Failed to send welcome email: {}", e.getMessage());
        }
        
        // Log audit event
        auditService.logAuditEvent("USER", userId, "CREATE", null, user, 
                getCurrentUserId(httpRequest), httpRequest);
        
        log.info("User created successfully: {} for tenant: {}", userId, tenantId);
        return user;
    }
    
    /**
     * Updates user information
     */
    @Transactional
    public User updateUser(String userId, UserRegistrationRequest request, HttpServletRequest httpRequest) {
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new BusinessValidationException("User not found"));
        
        User oldUser = User.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .status(user.getStatus())
                .role(user.getRole())
                .isAdmin(user.getIsAdmin())
                .build();
        
        // Update fields
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUpdatedAt(LocalDateTime.now());
        
        // Save updated user
        userRepository.save(user);
        
        // Log audit event
        auditService.logAuditEvent("USER", userId, "UPDATE", oldUser, user, 
                getCurrentUserId(httpRequest), httpRequest);
        
        return user;
    }
    
    /**
     * Deactivates a user
     */
    @Transactional
    public void deactivateUser(String userId, String reason, HttpServletRequest httpRequest) {
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new BusinessValidationException("User not found"));
        
        user.setStatus(User.UserStatus.INACTIVE);
        user.setEndDateEffective(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);
        
        // Update usage metrics
        //updateUserUsageMetrics(user.getTenant().getId());
        
        // Log audit event
        auditService.logAuditEvent("USER", userId, "DEACTIVATE", null, 
                "Reason: " + reason, getCurrentUserId(httpRequest), httpRequest);
        
        log.info("User deactivated: {} for tenant: {}", userId, user.getTenant().getId());
    }
    
    /**
     * Gets all users for a tenant
     */
    public List<User> getUsersByTenant(String tenantId) {
        return userRepository.findByTenantId(tenantId);
    }
    
    /**
     * Gets paginated users for a tenant
     */
    public Page<User> getUsersByTenant(String tenantId, Pageable pageable) {
        return userRepository.findByTenantId(tenantId, pageable);
    }
    
    /**
     * Gets user by ID
     */
    public Optional<User> getUserById(String userId) {
        return userRepository.findById(UUID.fromString(userId));
    }
    
    /**
     * Gets user by email and tenant
     */
    public Optional<User> getUserByEmailAndTenant(String email, String tenantId) {
        return userRepository.findByEmailAndTenantId(email, tenantId);
    }
    
    /**
     * Updates user usage metrics
     */
    @Transactional
    public void updateUserUsageMetrics(String tenantId) {
       
    }
    
    /**
     * Gets current user usage for a tenant
     */
    public UsageMetrics getCurrentUserUsage(String tenantId) {
        return null;
    }
    
    /**
     * Gets current user ID from security context
     */
    private String getCurrentUserId(HttpServletRequest request) {
        // TODO: Implement based on your security setup
        // This would typically get the user ID from JWT token or security context
        return "system";
    }
}
