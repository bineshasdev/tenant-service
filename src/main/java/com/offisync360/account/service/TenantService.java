package com.offisync360.account.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import com.offisync360.account.common.PasswordGenerator;
import com.offisync360.account.common.properties.AppProperties;
import com.offisync360.account.dto.TenantSignupRequest;
import com.offisync360.account.dto.TenantSignupResponse;
import com.offisync360.account.exception.BusinessValidationException;
import com.offisync360.account.model.SubscriptionPlan;
import com.offisync360.account.model.Tenant;
import com.offisync360.account.model.TenantConfig;
import com.offisync360.account.repository.SubscriptionPlanRepository;
import com.offisync360.account.repository.TenantConfigRepository;
import com.offisync360.account.repository.TenantRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final KeycloakAdminClient keycloakAdminClient;

    private final TenantRepository tenantRepository;

    private final PasswordGenerator passwordGenerator;

    private final AppProperties appProperties;

    private final NotificationService notificationService;

    private final TenantIdService tenantIdService;

    private final TenantConfigRepository tenantConfigRepository;

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    private static final List<String> RESERVED_TENANT_IDS = List.of(
    "admin", "system", "master", "keycloak", "auth", "api");

    @Transactional
    public TenantSignupResponse signupTenant(TenantSignupRequest request) {
        
        
        var companyName = request.getCompanyName();
        String realmName = tenantIdService.generateTenantId(companyName);
        realmName = tenantIdService.ensureUniqueTenantId(realmName, keycloakAdminClient::realmExists);
        validateSignupRequest(request, realmName);
       
        String uiClientId = appProperties.getClientIds().getUi();
        String apiClientId = appProperties.getClientIds().getApi();
        String adminPassword = passwordGenerator.generateStrongPassword();

        Tenant tenant = new Tenant();
        tenant.setId(realmName);
        tenant.setDisplayName(request.getDisplayName());
        tenant.setAdminEmail(request.getAdminEmail());
        tenant.setRealmName(realmName);
        tenant.setCreatedAt(LocalDateTime.now());
        tenant.setStartDateEffective(LocalDateTime.now());
        tenant.setSubscriptionPlan(subscriptionPlanRepository.findByCode(SubscriptionPlan.basic().getCode()).orElse(null) );
        tenant.setLocale(request.getLocale());
        tenant.setCountry(request.getCountry());
        tenant.setPhone(request.getMobileNumber());
        tenant.setAdminTempPassword(adminPassword);

        TenantConfig config = new TenantConfig();
        config.setServerUrl(keycloakAdminClient.getServerUrl());
        config.setApiClientId(apiClientId);
        config.setUiClientId(uiClientId);

        tenantRepository.save(tenant);
        config.setId(tenant.getId());
        
        tenantConfigRepository.save(config);


        // 1. Create Keycloak realm
        RealmRepresentation realm = keycloakAdminClient.createRealm(realmName, request.getDisplayName());
        
         // 2. Create roles
         keycloakAdminClient.createRealmRoles(realmName, List.of("user", "admin", "guest"));

        // 3. Create admin user
       
        UserRepresentation adminUser = keycloakAdminClient.createAdminUser(
                realmName,
                request.getAdminEmail(),
                adminPassword,
                request.getAdminFirstName(),
                request.getAdminLastName());

        // 4. Create API client
        ClientRepresentation apiClient = keycloakAdminClient.createClient(
                realmName,
                apiClientId,
                "API client for " + request.getDisplayName(),
                true);

        // 5. Create UI client
        ClientRepresentation uiClient = keycloakAdminClient.createClient(
                realmName,
                uiClientId,
                "UI client for " + request.getDisplayName(),
                false);

        
        config.setApiClientSecret(apiClient.getSecret());
        config.setUiClientSecret(uiClient.getSecret());
        tenantConfigRepository.save(config);

        notificationService.sendSignupStartedNotification();
        return TenantSignupResponse.builder()
                .tenantId(tenant.getId())
                .realmName(realmName) 
                .adminEmail(request.getAdminEmail())
                .apiClientId(apiClient.getClientId())
                .uiClientId(uiClient.getClientId())
                .build();
    }

    private void validateSignupRequest(TenantSignupRequest request, String tenantId) {
        List<String> errors = new ArrayList<>();

        if (RESERVED_TENANT_IDS.contains(request.getCompanyName().toLowerCase())) {
            errors.add("This tenant ID is reserved");
        }
        if (tenantRepository.existsById(tenantId)) {
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
    public Tenant updateSubscription(String tenantId, SubscriptionPlan newPlan) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        
        tenant.setSubscriptionPlan(newPlan);
        return tenantRepository.save(tenant);
    }
}