package com.offisync360.account.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.offisync360.account.dto.auth.ClientResponse;
import com.offisync360.account.dto.auth.UserResponse;

import com.offisync360.account.common.PasswordGenerator;
import com.offisync360.account.common.properties.AppProperties;
import com.offisync360.account.dto.TenantSignupRequest;
import com.offisync360.account.dto.TenantSignupResponse;
import com.offisync360.account.exception.BusinessValidationException;
import com.offisync360.account.model.Subscription;
import com.offisync360.account.model.SubscriptionPlan;
import com.offisync360.account.model.Tenant;
import com.offisync360.account.model.TenantSettings;
import com.offisync360.account.model.User.UserRole;
import com.offisync360.account.repository.SubscriptionPlanRepository;
import com.offisync360.account.repository.SubscriptionRepository;
import com.offisync360.account.repository.TenantRepository;
import com.offisync360.account.repository.UserRepository;
import com.offisync360.account.service.auth.AuthenticationProvider;
import com.offisync360.account.service.auth.AuthenticationProviderFactory;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantService {

    private final TenantRepository tenantRepository;

    private final PasswordGenerator passwordGenerator;

    private final AppProperties appProperties;

    private final NotificationService notificationService;

    private final TenantIdService tenantIdService;

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    private final SubscriptionRepository subscriptionRepository;
    
    private final AuthenticationProviderFactory authProviderFactory;
    
    private final UserService userService;

    private final UserRepository userRepository;

    private static final List<String> RESERVED_TENANT_IDS = List.of(
    "admin", "system", "master", "keycloak", "auth", "api");

    @Transactional
    public TenantSignupResponse registerTenant(TenantSignupRequest request) {
        log.info("Starting tenant registration for company: {}", request.getCompanyName());
        
        // ============= PHASE 1: VALIDATION & DATABASE SETUP =============
        
        var companyName = request.getCompanyName();
        // Generate tenant id using tenantIdService and ensure it is unique from company name
        String realmName = tenantIdService.generateTenantId(companyName);

        // Get tenant settings (you can load from database or use defaults)
        TenantSettings settings = TenantSettings.getDefaultSettings();
        
        // Get authentication provider
        AuthenticationProvider authProvider = authProviderFactory.getDefaultProvider();
        
        // Ensure tenant id is unique in authentication provider
        realmName = tenantIdService.ensureUniqueTenantId(realmName, authProvider::realmExists);
        
        // Validate signup request
        validateSignupRequest(request, realmName);
       
        String uiClientId = appProperties.getClientIds().getUi(); // client id for public client like ui
        String apiClientId = appProperties.getClientIds().getApi(); // client id for api client or backend client
        String adminPassword = request.getAdminPassword() != null && !request.getAdminPassword().isEmpty() 
                ? request.getAdminPassword() 
                : passwordGenerator.generateStrongPassword(); // password for admin user

        // 1. Save Tenant to Database (without auth provider details)
        log.info("Phase 1: Creating tenant in database with realm: {}", realmName);
        Tenant tenant = new Tenant();
        tenant.setId(UUID.randomUUID().toString());
        tenant.setDisplayName(request.getDisplayName());
        tenant.setAdminEmail(request.getAdminEmail());
        tenant.setRealmName(realmName); // realm name for keycloak
        tenant.setStartDateEffective(LocalDateTime.now()); 
        tenant.setLocale(request.getLocale());
        tenant.setCountry(request.getCountry());
        tenant.setPhone(request.getMobileNumber());
        tenant.setAdminTempPassword(adminPassword);
        tenant.setClientId(apiClientId);
        tenant.setPublicClientId(uiClientId);
        tenant.setStatus("PROVISIONING"); // Mark as provisioning
        tenant = tenantRepository.save(tenant);
        log.info("Tenant saved to database with ID: {}", tenant.getId());

        // 2. Create Subscription in Database
        log.info("Phase 1: Creating subscription for tenant: {}", tenant.getId());
        Subscription subscription = new Subscription();
        subscription.setId(UUID.randomUUID());
        subscription.setTenant(tenant);
        
        // If no plan id in request, set to free plan
        SubscriptionPlan plan;
        if (request.getSubscriptionId() == null) {
            plan = subscriptionPlanRepository.findByCode("FREE").orElse(null);
        } else {
            plan = subscriptionPlanRepository.findById(request.getSubscriptionId()).orElse(null);
        }
        
        subscription.setPlan(plan); 
        subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
        subscription.setBillingCycle(Subscription.BillingCycle.MONTHLY);
        subscription.setCurrentPrice(plan.getMonthlyPrice() == null ? BigDecimal.ZERO : plan.getMonthlyPrice());
        subscription.setStartDate(LocalDateTime.now());
        subscription.setEndDate(LocalDateTime.now().plusYears(1)); 
        subscription.setTrialEndDate(LocalDateTime.now().plusDays(14));
        subscription.setAutoRenew(true);
        subscription = subscriptionRepository.save(subscription);
        log.info("Subscription saved with ID: {}", subscription.getId());

        // ============= PHASE 2: AUTH PROVIDER PROVISIONING =============
        
        UserResponse adminUser = null;
        ClientResponse apiClient = null;
        ClientResponse uiClient = null;
        
        try {
            log.info("Phase 2: Starting auth provider provisioning for realm: {}", realmName);
            
            // 1. Create realm using authentication provider
            log.info("Creating realm in auth provider: {}", realmName);
            authProvider.createRealm(realmName, request.getDisplayName(), settings);
            
            // 2. Create roles using authentication provider
            log.info("Creating realm roles in auth provider");
            authProvider.createRealmRoles(realmName, List.of("user", "admin", "guest"), settings);

            // 3. Create admin user using authentication provider
            log.info("Creating admin user in auth provider: {}", request.getAdminEmail());
            adminUser = authProvider.createAdminUser(
                    realmName,
                    request.getAdminEmail(),
                    adminPassword,
                    request.getAdminFirstName(),
                    request.getAdminLastName(),
                    settings, 
                    (request.getAdminPassword() == null || request.getAdminPassword().isEmpty()));

            // 4. Create API client using authentication provider
            log.info("Creating API client in auth provider: {}", apiClientId);
            apiClient = authProvider.createClient(
                    realmName,
                    apiClientId,
                    "API client for " + request.getDisplayName(),
                    true,
                    settings);

            // 5. Create UI client using authentication provider
            log.info("Creating UI client in auth provider: {}", uiClientId);
            uiClient = authProvider.createClient(
                    realmName,
                    uiClientId,
                    "UI client for " + request.getDisplayName(),
                    false,
                    settings);
                    
            log.info("Phase 2: Auth provider provisioning completed successfully");
            
        } catch (Exception e) {
            log.error("Phase 2: Auth provider provisioning failed for tenant: {}, error: {}", 
                     tenant.getId(), e.getMessage(), e);
            
            // Mark tenant as failed provisioning
            tenant.setStatus("PROVISIONING_FAILED");
            tenantRepository.save(tenant);
            
            // Optionally: You can choose to either:
            // 1. Keep the DB records and allow retry/manual fix
            // 2. Rollback by deleting tenant and subscription
            // For now, keeping records for potential retry
            
            throw new BusinessValidationException(
                "Failed to provision authentication resources: " + e.getMessage());
        }

        // ============= PHASE 3: UPDATE DATABASE WITH AUTH PROVIDER DETAILS =============
        
        try {
            log.info("Phase 3: Updating tenant with auth provider details");
            
            // Update tenant with auth provider secrets
            tenant.setClientSecret(apiClient.getClientSecret());
            tenant.setPublicClientSecret(uiClient.getClientSecret());
            tenant.setStatus("ACTIVE"); // Mark as active now
            tenant = tenantRepository.save(tenant);
            log.info("Tenant updated with client secrets and marked as ACTIVE");

            // 6. Create admin user in database
            log.info("Phase 3: Creating admin user in database");
            try {
                userService.createUser(
                    tenant.getId(),
                    request.getAdminEmail(),
                    request.getAdminFirstName(),
                    request.getAdminLastName(),
                    UserRole.ADMIN,
                    adminUser.getUserId(),
                    adminUser.getUsername()
                );
                log.info("Admin user created in database for tenant: {}", tenant.getId());
            } catch (Exception e) {
                log.error("Failed to create admin user in database for tenant: {}, error: {}", 
                         tenant.getId(), e.getMessage());
                // Don't fail the entire signup process if user creation fails
                // The user can be created later through the admin interface
            }

            // Send notification
            log.info("Sending signup notification");
            notificationService.sendSignupStartedNotification();
            
            log.info("Tenant registration completed successfully for tenant: {}", tenant.getId());
            
            return TenantSignupResponse.builder()
                    .tenantId(tenant.getId().toString())
                    .realmName(realmName) 
                    .adminEmail(request.getAdminEmail())
                    .apiClientId(apiClient.getClientId())
                    .uiClientId(uiClient.getClientId())
                    .build();
                    
        } catch (Exception e) {
            log.error("Phase 3: Failed to update database with auth provider details: {}", e.getMessage(), e);
            
            // Mark tenant as needs attention
            tenant.setStatus("INCOMPLETE");
            tenantRepository.save(tenant);
            
            throw new BusinessValidationException(
                "Tenant created in auth provider but failed to update database: " + e.getMessage());
        }
    }
 

    @Transactional
    public void updateSubscription(String tenantId, UUID planId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        // Make existing subscription inactive (if any active/trial subscription exists)
        subscriptionRepository.findActiveSubscriptionByTenantId(tenantId).ifPresent(existingSub -> {
            existingSub.setStatus(Subscription.SubscriptionStatus.CANCELLED);
            subscriptionRepository.save(existingSub);
        });

        // Set the new subscription as ACTIVE
        // Create new subscription based on the plan id
        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Subscription plan not found"));

        Subscription newSubscription = new Subscription();
        newSubscription.setTenant(tenant);
        newSubscription.setPlan(plan);
        newSubscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
        newSubscription.setBillingCycle(Subscription.BillingCycle.MONTHLY);
        newSubscription.setCurrentPrice(plan.getMonthlyPrice());
        newSubscription.setStartDate(LocalDateTime.now());
        newSubscription.setEndDate(LocalDateTime.now().plusYears(1));
        newSubscription.setTrialEndDate(LocalDateTime.now().plusDays(14));
        newSubscription.setAutoRenew(true);

        subscriptionRepository.save(newSubscription);
 
    }

    public Tenant findTenantByUsernameOrEmail(String usernameOrEmail) {
        // Try to find by admin email first
        Optional<Tenant> tenantByEmail = userRepository.findFirstTenantByUsernameOrEmail(usernameOrEmail);
        if(tenantByEmail.isEmpty()) {
            throw new BusinessValidationException("Tenant not found");
        }

        return tenantByEmail.get();
    }
    

    private void validateSignupRequest(TenantSignupRequest request, String tenantId) {
        List<String> errors = new ArrayList<>();

        if (RESERVED_TENANT_IDS.contains(request.getCompanyName().toLowerCase())) {
            errors.add("This tenant ID is reserved");
        }
        if (tenantRepository.existsByRealmName(tenantId)) {
                errors.add("Tenant ID '" + tenantId + "' is already taken");
        }

        if (!isValidEmailDomain(request.getAdminEmail())) {
            errors.add("Only corporate email addresses from approved domains are allowed");
        }

        if (tenantRepository.existsByAdminEmail(request.getAdminEmail())) {
            errors.add("Email '" + request.getAdminEmail() + "' is already registered as an admin");
        }

        if (!isValidKeycloakRealmName(tenantId)) {
            errors.add("Tenant ID can only contain letters, numbers, hyphens and underscores");
        }

        if (containsProfanity(request.getDisplayName())) {
            errors.add("Display name contains inappropriate content");
        }

        if (request.getDefaultRoles() != null && request.getDefaultRoles().contains("super-admin")) {
            errors.add("Cannot create tenant with 'super-admin' role");
        }

        if (!errors.isEmpty()) {
            throw new BusinessValidationException(String.join(", ", errors));
        }
    }
 

    private boolean isValidEmailDomain(String email) {
        // Implement your domain validation logic
        String[] allowedDomains = { "test.com", "test123.com" };
        String domain = email.substring(email.indexOf('@') + 1);
        return !Arrays.asList(allowedDomains).contains(domain);
    }

    private boolean isValidKeycloakRealmName(String realmName) {
        // Keycloak realm names must match this regex
        return realmName.matches("^[a-zA-Z0-9-_]+$");
    }

    private boolean containsProfanity(String text) {
        // Implement profanity filter (could use a library or external service)
        String[] blockedTerms = { "badword1", "badword2" };
        String lowercaseText = text.toLowerCase();
        return Arrays.stream(blockedTerms)
                .anyMatch(lowercaseText::contains);
    }
   
}