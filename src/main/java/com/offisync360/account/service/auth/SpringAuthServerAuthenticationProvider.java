package com.offisync360.account.service.auth;

import com.offisync360.account.dto.auth.ClientResponse;
import com.offisync360.account.dto.auth.RealmResponse;
import com.offisync360.account.dto.auth.UserResponse;
import com.offisync360.account.model.TenantSettings;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpringAuthServerAuthenticationProvider implements AuthenticationProvider {

    @Override
    public RealmResponse createRealm(String realmName, String displayName, TenantSettings settings) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createRealm'");
    }

    @Override
    public UserResponse createAdminUser(String realmName, String email, String password, String firstName,
            String lastName, TenantSettings settings, boolean resetPassword) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createAdminUser'");
    }

    @Override
    public UserResponse createUser(String realmName, String email, String password, String firstName, String lastName,
            TenantSettings settings, boolean resetPassword) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createUser'");
    }

    @Override
    public ClientResponse createClient(String realmName, String clientId, String clientName, boolean confidential,
            TenantSettings settings) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createClient'");
    }

    @Override
    public void createRealmRoles(String realmName, List<String> roleNames, TenantSettings settings) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createRealmRoles'");
    }

    @Override
    public boolean realmExists(String realmName) {
        // TODO: Implement Spring Auth Server realm existence check
        throw new UnsupportedOperationException("Spring Auth Server implementation not yet available");
    }

    @Override
    public int getUserCount(String realmName) {
        // TODO: Implement Spring Auth Server user count
        // Example: GET https://{domain}/api/v2/users with pagination
        throw new UnsupportedOperationException("Spring Auth Server implementation not yet available");
    }

    @Override
    public String getProviderType() {
        
        return "SPRING_AUTH_SERVER";
    }
  
}
