package com.offisync360.account.service.auth;

import com.offisync360.account.dto.auth.ClientResponse;
import com.offisync360.account.dto.auth.RealmResponse;
import com.offisync360.account.dto.auth.UserResponse;
import com.offisync360.account.model.TenantSettings;
import com.offisync360.account.service.KeycloakAdminClient;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Keycloak implementation of the AuthenticationProvider interface
 */
@Component
public class KeycloakAuthenticationProvider implements AuthenticationProvider {
    
    private final KeycloakAdminClient keycloakAdminClient;
    
    public KeycloakAuthenticationProvider(KeycloakAdminClient keycloakAdminClient) {
        this.keycloakAdminClient = keycloakAdminClient;
    }
    
    @Override
    public RealmResponse createRealm(String realmName, String displayName, TenantSettings settings) {
        RealmRepresentation keycloakRealm = keycloakAdminClient.createRealm(realmName, displayName, settings);
        
        // Convert Keycloak-specific response to generic response
        return RealmResponse.builder()
                .realmId(keycloakRealm.getId())
                .realmName(keycloakRealm.getRealm())
                .displayName(keycloakRealm.getDisplayName())
                .enabled(keycloakRealm.isEnabled() != null ? keycloakRealm.isEnabled() : true)
                .authServerUrl(keycloakAdminClient.getServerUrl())
                .providerSpecificData(keycloakRealm) // Store original for advanced usage
                .build();
    }
    
    @Override
    public UserResponse createAdminUser(String realmName, String email, String password, 
                                            String firstName, String lastName, TenantSettings settings, boolean resetPassword) {
        UserRepresentation keycloakUser = keycloakAdminClient.createUser(
                realmName, email, password, firstName, lastName, resetPassword, "admin");
        
        // Convert Keycloak-specific response to generic response
        return convertToUserResponse(keycloakUser);
    }
    
    @Override
    public UserResponse createUser(String realmName, String email, String password, String firstName,
            String lastName, TenantSettings settings, boolean resetPassword) {
        UserRepresentation keycloakUser = keycloakAdminClient.createUser(
                realmName, email, password, firstName, lastName, resetPassword, "user");
        
        // Convert Keycloak-specific response to generic response
        return convertToUserResponse(keycloakUser);
    }

    @Override
    public ClientResponse createClient(String realmName, String clientId, String clientName, 
                                           boolean confidential, TenantSettings settings) {
        ClientRepresentation keycloakClient = keycloakAdminClient.createClient(
                realmName, clientId, clientName, confidential);
        
        // Convert Keycloak-specific response to generic response
        return ClientResponse.builder()
                .clientId(keycloakClient.getClientId())
                .clientName(keycloakClient.getName())
                .clientSecret(keycloakClient.getSecret())
                .confidential(!keycloakClient.isPublicClient())
                .enabled(keycloakClient.isEnabled() != null ? keycloakClient.isEnabled() : true)
                .redirectUris(keycloakClient.getRedirectUris())
                .webOrigins(keycloakClient.getWebOrigins())
                .directAccessGrantsEnabled(keycloakClient.isDirectAccessGrantsEnabled() != null ? 
                        keycloakClient.isDirectAccessGrantsEnabled() : false)
                .standardFlowEnabled(keycloakClient.isStandardFlowEnabled() != null ? 
                        keycloakClient.isStandardFlowEnabled() : false)
                .serviceAccountsEnabled(keycloakClient.isServiceAccountsEnabled() != null ? 
                        keycloakClient.isServiceAccountsEnabled() : false)
                .providerSpecificData(keycloakClient) // Store original for advanced usage
                .build();
    }
    
    @Override
    public void createRealmRoles(String realmName, List<String> roleNames, TenantSettings settings) {
        keycloakAdminClient.createRealmRoles(realmName, roleNames);
    }
    
    @Override
    public boolean realmExists(String realmName) {
        return keycloakAdminClient.realmExists(realmName);
    }
    
    @Override
    public int getUserCount(String realmName) {
        return keycloakAdminClient.getUserCount(realmName);
    }
    
    @Override
    public String getProviderType() {
        return "KEYCLOAK";
    }
    
    /**
     * Helper method to convert Keycloak UserRepresentation to generic UserResponse
     */
    private UserResponse convertToUserResponse(UserRepresentation keycloakUser) {
        return UserResponse.builder()
                .userId(keycloakUser.getId())
                .username(keycloakUser.getUsername())
                .email(keycloakUser.getEmail())
                .firstName(keycloakUser.getFirstName())
                .lastName(keycloakUser.getLastName())
                .enabled(keycloakUser.isEnabled() != null ? keycloakUser.isEnabled() : true)
                .emailVerified(keycloakUser.isEmailVerified() != null ? keycloakUser.isEmailVerified() : false)
                .roles(List.of()) // Roles would need to be fetched separately in Keycloak
                .requiredActions(keycloakUser.getRequiredActions())
                .providerSpecificData(keycloakUser) // Store original for advanced usage
                .build();
    }
}
