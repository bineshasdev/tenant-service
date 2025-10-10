package com.offisync360.account.service.auth;

import com.offisync360.account.model.TenantSettings;
import com.offisync360.account.service.KeycloakAdminClient;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RequiredActionProviderRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class KeycloakAuthenticationProvider implements AuthenticationProvider {
    
    private final KeycloakAdminClient keycloakAdminClient;
    
    public KeycloakAuthenticationProvider(KeycloakAdminClient keycloakAdminClient) {
        this.keycloakAdminClient = keycloakAdminClient;
    }
    
    @Override
    public RealmRepresentation createRealm(String realmName, String displayName, TenantSettings settings) {
        RealmRepresentation realm = keycloakAdminClient.createRealm(realmName, displayName);
        
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
        
        return realm;
    }
    
    @Override
    public UserRepresentation createAdminUser(String realmName, String email, String password, 
                                            String firstName, String lastName, TenantSettings settings, boolean resetPassword) {
        return keycloakAdminClient.createUser(realmName, email, password, firstName, lastName, resetPassword, "admin");
    }
    
    @Override
    public UserRepresentation createUser(String realmName, String email, String password, String firstName,
            String lastName, TenantSettings settings, boolean resetPassword) {
         
        return keycloakAdminClient.createUser(realmName, email, password, firstName, lastName, resetPassword, "user");
    }

    @Override
    public ClientRepresentation createClient(String realmName, String clientId, String clientName, 
                                           boolean confidential, TenantSettings settings) {
        return keycloakAdminClient.createClient(realmName, clientId, clientName, confidential);
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
}
