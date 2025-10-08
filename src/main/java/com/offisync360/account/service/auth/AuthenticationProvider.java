package com.offisync360.account.service.auth;

import com.offisync360.account.model.TenantSettings;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

public interface AuthenticationProvider {

    /**
     * Create a realm/tenant in the authentication provider
     */
    RealmRepresentation createRealm(String realmName, String displayName, TenantSettings settings);

    /**
     * Create an admin user in the realm
     */
    UserRepresentation createAdminUser(String realmName, String email, String password,
            String firstName, String lastName, TenantSettings settings, boolean resetPassword);

  
    UserRepresentation createUser(String realmName, String email, String password,
            String firstName, String lastName, TenantSettings settings, boolean resetPassword);

    /**
     * Create a client/application in the realm
     */
    ClientRepresentation createClient(String realmName, String clientId, String clientName,
            boolean confidential, TenantSettings settings);

    /**
     * Create realm roles
     */
    void createRealmRoles(String realmName, java.util.List<String> roleNames, TenantSettings settings);

    /**
     * Check if realm exists
     */
    boolean realmExists(String realmName);

    /**
     * Get user count for the realm
     */
    int getUserCount(String realmName);

    /**
     * Get the provider type
     */
    String getProviderType();
}
