package com.offisync360.account.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import com.offisync360.account.common.PasswordGenerator;
import com.offisync360.account.common.properties.AppProperties;
import com.offisync360.account.dto.TenantSignupRequest;
import com.offisync360.account.dto.TenantSignupResponse;
import com.offisync360.account.exception.BusinessValidationException;
import com.offisync360.account.model.Subscription;
import com.offisync360.account.model.SubscriptionPlan;
import com.offisync360.account.model.Tenant;
import com.offisync360.account.model.TenantSettings;
import com.offisync360.account.repository.SubscriptionPlanRepository;
import com.offisync360.account.repository.SubscriptionRepository;
import com.offisync360.account.repository.TenantRepository;
import com.offisync360.account.service.auth.AuthenticationProvider;
import com.offisync360.account.service.auth.AuthenticationProviderFactory;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;

    private final PasswordGenerator passwordGenerator;

    private final AppProperties appProperties;

    private final NotificationService notificationService;

    private final TenantIdService tenantIdService;

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    private final SubscriptionRepository subscriptionRepository;
    
    private final AuthenticationProviderFactory authProviderFactory;

    private static final List<String> RESERVED_TENANT_IDS = List.of(
    "admin", "system", "master", "keycloak", "auth", "api");

    @Transactional
    public TenantSignupResponse registerTenant(TenantSignupRequest request) {
        
        
        var companyName = request.getCompanyName();
        //Generate tenant id using tenantIdService and ensure it is unique from company name
        String realmName = tenantIdService.generateTenantId(companyName);

        // Get tenant settings (you can load from database or use defaults)
        TenantSettings settings = TenantSettings.getDefaultSettings();
        
        // Get authentication provider
        AuthenticationProvider authProvider = authProviderFactory.getDefaultProvider();
        
        //Ensure tenant id is unique in authentication provider
        realmName = tenantIdService.ensureUniqueTenantId(realmName, authProvider::realmExists);
        validateSignupRequest(request, realmName);
       
        String uiClientId = appProperties.getClientIds().getUi(); // client id for public client like ui
        String apiClientId = appProperties.getClientIds().getApi(); // client id for api client or backend client
        String adminPassword = passwordGenerator.generateStrongPassword(); // password for admin user

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
        tenantRepository.save(tenant);


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

        subscriptionRepository.save(subscription);
         


        // 1. Create realm using authentication provider
        RealmRepresentation realm = authProvider.createRealm(realmName, request.getDisplayName(), settings);
        
        // 2. Create roles using authentication provider
        authProvider.createRealmRoles(realmName, List.of("user", "admin", "guest"), settings);

        // 3. Create admin user using authentication provider
        UserRepresentation adminUser = authProvider.createAdminUser(
                realmName,
                request.getAdminEmail(),
                adminPassword,
                request.getAdminFirstName(),
                request.getAdminLastName(),
                settings);

        // 4. Create API client using authentication provider
        ClientRepresentation apiClient = authProvider.createClient(
                realmName,
                apiClientId,
                "API client for " + request.getDisplayName(),
                true,
                settings);

        // 5. Create UI client using authentication provider
        ClientRepresentation uiClient = authProvider.createClient(
                realmName,
                uiClientId,
                "UI client for " + request.getDisplayName(),
                false,
                settings);

        
        tenant.setClientSecret(apiClient.getSecret());
        tenant.setPublicClientSecret(uiClient.getSecret());
        tenantRepository.save(tenant);

        notificationService.sendSignupStartedNotification();
        return TenantSignupResponse.builder()
                .tenantId(tenant.getId().toString())
                .realmName(realmName) 
                .adminEmail(request.getAdminEmail())
                .apiClientId(apiClient.getClientId())
                .uiClientId(uiClient.getClientId())
                .build();
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