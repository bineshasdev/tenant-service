package com.offisync360.account.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class TenantSignupResponse {

    private String tenantId;
    private String displayName;
    private String realmName;
    private String adminEmail;
    
    // Keycloak configuration
    private String keycloakServerUrl;
    private String realmUrl;
    private String adminConsoleUrl;
    
    // Client information
    private String apiClientId;
    private String uiClientId;
    
    // Timestamps
    private Instant createdAt;
    private Instant expiresAt; // If you want to implement trial periods
    
    // Status information
    private String status;
    private String message;
    
    // Links for the new tenant admin
    private String adminLoginUrl;
    private String apiDocumentationUrl;
    
    // You might want to include this only in the initial response
    @Builder.Default
    private boolean initialSetupComplete = true;
}