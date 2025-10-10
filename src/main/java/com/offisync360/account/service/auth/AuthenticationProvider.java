package com.offisync360.account.service.auth;

import com.offisync360.account.dto.auth.ClientResponse;
import com.offisync360.account.dto.auth.RealmResponse;
import com.offisync360.account.dto.auth.UserResponse;
import com.offisync360.account.model.TenantSettings;

import java.util.List;

/**
 * Generic authentication provider interface that can be implemented by
 * different authentication services (Keycloak, Auth0, AWS Cognito, etc.)
 */
public interface AuthenticationProvider {

    /**
     * Create a realm/tenant in the authentication provider
     * 
     * @param realmName The unique name for the realm
     * @param displayName The display name for the realm
     * @param settings Tenant-specific settings to apply
     * @return Generic realm response
     */
    RealmResponse createRealm(String realmName, String displayName, TenantSettings settings);

    /**
     * Create an admin user in the realm
     * 
     * @param realmName The realm to create the user in
     * @param email User's email address
     * @param password User's password
     * @param firstName User's first name
     * @param lastName User's last name
     * @param settings Tenant-specific settings
     * @param resetPassword Whether user should reset password on first login
     * @return Generic user response
     */
    UserResponse createAdminUser(String realmName, String email, String password,
            String firstName, String lastName, TenantSettings settings, boolean resetPassword);

    /**
     * Create a regular user in the realm
     * 
     * @param realmName The realm to create the user in
     * @param email User's email address
     * @param password User's password
     * @param firstName User's first name
     * @param lastName User's last name
     * @param settings Tenant-specific settings
     * @param resetPassword Whether user should reset password on first login
     * @return Generic user response
     */
    UserResponse createUser(String realmName, String email, String password,
            String firstName, String lastName, TenantSettings settings, boolean resetPassword);

    /**
     * Create a client/application in the realm
     * 
     * @param realmName The realm to create the client in
     * @param clientId The unique client identifier
     * @param clientName The display name for the client
     * @param confidential Whether this is a confidential client (vs public)
     * @param settings Tenant-specific settings
     * @return Generic client response
     */
    ClientResponse createClient(String realmName, String clientId, String clientName,
            boolean confidential, TenantSettings settings);

    /**
     * Create realm roles
     * 
     * @param realmName The realm to create roles in
     * @param roleNames List of role names to create
     * @param settings Tenant-specific settings
     */
    void createRealmRoles(String realmName, List<String> roleNames, TenantSettings settings);

    /**
     * Check if realm exists
     * 
     * @param realmName The realm name to check
     * @return true if realm exists, false otherwise
     */
    boolean realmExists(String realmName);

    /**
     * Get user count for the realm
     * 
     * @param realmName The realm to count users in
     * @return Number of users in the realm
     */
    int getUserCount(String realmName);

    /**
     * Get the provider type
     * 
     * @return Provider type identifier (KEYCLOAK, AUTH0, AWS_COGNITO, etc.)
     */
    String getProviderType();
}
