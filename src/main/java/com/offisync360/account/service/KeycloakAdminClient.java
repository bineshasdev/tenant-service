package com.offisync360.account.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource; 
import org.keycloak.representations.idm.ClientRepresentation; 
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RequiredActionProviderRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.offisync360.account.model.TenantSettings;

import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KeycloakAdminClient { 

    @Value("${keycloak.auth-server-url}")
    private String serverUrl;
    @Value("${keycloak.master-realm}")
    private String masterRealm;
    @Value("${keycloak.admin-client-id}")
    private String clientId;
    @Value("${keycloak.admin-client-secret}")
    private String clientSecret;
    @Value("${keycloak.admin-username}")
    private String username;
    @Value("${keycloak.admin-password}")
    private String password;


    public RealmRepresentation createRealm(String realmName, String displayName, TenantSettings settings) {
        Keycloak keycloak = getAdminKeycloakInstance();
        
        RealmRepresentation realm = new RealmRepresentation();
        realm.setRealm(realmName);
        realm.setDisplayName(displayName);
        realm.setEnabled(true);
        realm.setRegistrationAllowed(false);
        realm.setRememberMe(true);
        // Apply tenant settings to realm
        if (settings.getAccessTokenLifespan() != null) {
            realm.setAccessTokenLifespan(settings.getAccessTokenLifespan());
        }
        if (settings.getSsoSessionIdleTimeout() != null) {
            realm.setSsoSessionIdleTimeout(settings.getSsoSessionIdleTimeout());
        }
        if (settings.getSsoSessionMaxLifespan() != null) {
            realm.setSsoSessionMaxLifespan(settings.getSsoSessionMaxLifespan());
        }
        
        // Email as Username Configuration
        if (settings.getAllowEmailAsUsername() != null && settings.getAllowEmailAsUsername()) {
            realm.setLoginWithEmailAllowed(true);
            realm.setRegistrationEmailAsUsername(true); // This is critical for email as username
            realm.setDuplicateEmailsAllowed(false); // Ensure emails are unique
        }
        
        if (settings.getEmailVerificationRequired() != null) {
            realm.setVerifyEmail(settings.getEmailVerificationRequired());
        }
        if (settings.getRegistrationAllowed() != null) {
            realm.setRegistrationAllowed(settings.getRegistrationAllowed());
        }
        if (settings.getRememberMe() != null) {
            realm.setRememberMe(settings.getRememberMe());
        }
        
        // Enable Forgot Password / Reset Password functionality
        realm.setResetPasswordAllowed(true);
        
        // Configure required actions for password reset
        var updatePasswordAction = new RequiredActionProviderRepresentation();
        updatePasswordAction.setEnabled(true);
        updatePasswordAction.setName("UPDATE_PASSWORD");
        updatePasswordAction.setAlias("UPDATE_PASSWORD");
        
        // Enable additional required actions
        var verifyEmailAction = new RequiredActionProviderRepresentation();
        verifyEmailAction.setEnabled(true);
        verifyEmailAction.setName("VERIFY_EMAIL");
        verifyEmailAction.setAlias("VERIFY_EMAIL");
        
        // Set all required actions (don't override!)
        realm.setRequiredActions(List.of(updatePasswordAction, verifyEmailAction));
        
        keycloak.realms().create(realm);
        return realm;
    }

     

    public UserRepresentation createUser(String realmName, String email, 
                                          String password, String firstName, String lastName,
                                           boolean resetPassword, String role) {
        Keycloak keycloak = getAdminKeycloakInstance();
        
        // Create user
        UserRepresentation user = new UserRepresentation();
        user.setUsername(email); // Use email as username
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);
        user.setEmailVerified(true); // Mark email as verified initially
        
        // Set password
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(resetPassword); // If true, user must change password on first login
        
        user.setCredentials(List.of(credential));
        
        // Add required actions if password needs to be reset
        if (resetPassword) {
            user.setRequiredActions(List.of("UPDATE_PASSWORD"));
        }
        
        Response response = keycloak.realm(realmName).users().create(user);
        String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
        
        // Update user object with generated ID
        user.setId(userId);
        
        // Assign role to user
        if (role.equals("admin")) {
            RoleRepresentation adminRole = keycloak.realm(realmName).roles().get("admin").toRepresentation();
            keycloak.realm(realmName).users().get(userId).roles().realmLevel().add(List.of(adminRole));
        } else {
            RoleRepresentation userRole = keycloak.realm(realmName).roles().get("user").toRepresentation();
            keycloak.realm(realmName).users().get(userId).roles().realmLevel().add(List.of(userRole));
        }
        
        log.info("User created successfully in Keycloak: {} with role: {}", email, role);
        return user;
    }

    public ClientRepresentation createClient(String realmName, String clientId, 
                                           String clientName, boolean confidential) {
        Keycloak keycloak = getAdminKeycloakInstance();
        
        ClientRepresentation client = new ClientRepresentation();
        client.setClientId(clientId);
        client.setName(clientName);
        client.setConsentRequired(false);
        client.setProtocol("openid-connect");
        client.setPublicClient(!confidential);
        client.setDirectAccessGrantsEnabled(true);
        client.setStandardFlowEnabled(true);
        client.setServiceAccountsEnabled(true);
        if (confidential) {
            client.setSecret(UUID.randomUUID().toString());
        }
        
        // Default configuration
        client.setRedirectUris(List.of("*"));
        client.setWebOrigins(List.of("*"));
        client.setAttributes(Map.of(
            "post.logout.redirect.uris", "+",
            "exclude.session.state.from.auth.response", "false"
        ));
        
        keycloak.realm(realmName).clients().create(client);
        return client;
    }

    public void createRealmRoles(String realmName, List<String> roleNames) {
        Keycloak keycloak = getAdminKeycloakInstance();
        
        roleNames.forEach(roleName -> {
            RoleRepresentation role = new RoleRepresentation();
            role.setName(roleName);
            keycloak.realm(realmName).roles().create(role);
        });
    }

    private Keycloak getAdminKeycloakInstance() {
        return KeycloakBuilder.builder()
            .serverUrl(serverUrl) 
            .realm(masterRealm)
            .clientId(clientId)
            .username(username)
            .password(password)
            .build();
    }
 
    public String getServerUrl() {
        return serverUrl;
    }

    public boolean realmExists(String realm) {
        Keycloak keycloak = getAdminKeycloakInstance();
         try {
            RealmResource realmResource = keycloak.realm(realm);
            
            realmResource.toRepresentation();
            return true;
        } catch (Exception e) {
            log.error("Realm check error", e);
            return false;
        }
      
    }

    public int getUserCount(String realmName) {
        Keycloak keycloak = getAdminKeycloakInstance();
        try {
            return keycloak.realm(realmName).users().count();
        } catch (Exception e) {
            log.error("Error getting user count for realm {}: {}", realmName, e.getMessage());
            return 0;
        }
    }
}