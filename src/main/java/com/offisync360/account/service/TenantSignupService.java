package com.offisync360.account.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import com.offisync360.account.common.PasswordGenerator;
import com.offisync360.account.dto.TenantSignupRequest;
import com.offisync360.account.dto.TenantSignupResponse;
import com.offisync360.account.exception.BusinessValidationException;
import com.offisync360.account.model.Tenant;
import com.offisync360.account.model.TenantConfig;
import com.offisync360.account.repository.TenantRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TenantSignupService {

    private final KeycloakAdminClient keycloakAdminClient;
    private final TenantRepository tenantRepository;

    private final PasswordGenerator passwordGenerator;

    private static final List<String> RESERVED_TENANT_IDS = List.of(
    "admin", "system", "master", "keycloak", "auth", "api");

    @Transactional
    public TenantSignupResponse signupTenant(TenantSignupRequest request) {
        // 1. Validate request
        validateSignupRequest(request);

        // 2. Create Keycloak realm
        String realmName = request.getTenantId();
        RealmRepresentation realm = keycloakAdminClient.createRealm(realmName, request.getDisplayName());

        // 3. Create admin user
        String adminPassword = passwordGenerator.generateStrongPassword();
        UserRepresentation adminUser = keycloakAdminClient.createAdminUser(
                realmName,
                request.getAdminEmail(),
                adminPassword,
                request.getAdminFirstName(),
                request.getAdminLastName());

        // 4. Create API client
        ClientRepresentation apiClient = keycloakAdminClient.createClient(
                realmName,
                request.getTenantId() + "-api",
                "API client for " + request.getDisplayName(),
                true);

        // 5. Create UI client
        ClientRepresentation uiClient = keycloakAdminClient.createClient(
                realmName,
                request.getTenantId() + "-ui",
                "UI client for " + request.getDisplayName(),
                false);

        // 6. Create roles
        keycloakAdminClient.createRealmRoles(realmName, List.of("user", "admin", "super-admin"));

        // 7. Save to database
        Tenant tenant = new Tenant();
        tenant.setId(request.getTenantId());
        tenant.setDisplayName(request.getDisplayName());
        tenant.setAdminEmail(request.getAdminEmail());
        tenant.setRealmName(realmName);

        TenantConfig config = new TenantConfig();
        config.setServerUrl(keycloakAdminClient.getServerUrl());
        config.setApiClientId(apiClient.getClientId());
        config.setUiClientId(uiClient.getClientId());

        config.setApiClientSecret(apiClient.getSecret());
        config.setUiClientSecret(uiClient.getSecret());

        tenantRepository.save(tenant);

        // 8. Send email notification
        /*
         * emailService.sendTenantSetupEmail(
         * request.getAdminEmail(),
         * request.getDisplayName(),
         * realmName,
         * request.getAdminEmail(),
         * adminPassword,
         * keycloakAdminClient.getServerUrl() + "/realms/" + realmName + "/account"
         * );
         */

        return TenantSignupResponse.builder()
                .tenantId(tenant.getId())
                .realmName(realmName)
                .adminEmail(request.getAdminEmail())
                .apiClientId(apiClient.getClientId())
                .uiClientId(uiClient.getClientId())
                .build();
    }

    private void validateSignupRequest(TenantSignupRequest request) {
        List<String> errors = new ArrayList<>();

        if (RESERVED_TENANT_IDS.contains(request.getTenantId().toLowerCase())) {
            errors.add("This tenant ID is reserved");
        }
        if (tenantRepository.existsById(request.getTenantId())) {
            errors.add("Tenant ID '" + request.getTenantId() + "' is already taken");
        }

        if (!isValidEmailDomain(request.getAdminEmail())) {
            errors.add("Only corporate email addresses from approved domains are allowed");
        }

        if (tenantRepository.existsByAdminEmail(request.getAdminEmail())) {
            errors.add("Email '" + request.getAdminEmail() + "' is already registered as an admin");
        }

        if (!isValidKeycloakRealmName(request.getTenantId())) {
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