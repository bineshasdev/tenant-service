package com.offisync360.account.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic response for user creation across different authentication providers
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    
    private String userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private boolean enabled;
    private boolean emailVerified;
    private List<String> roles;
    private List<String> requiredActions;
    
    // Provider-specific data can be stored here
    private Object providerSpecificData;
    
    /**
     * Get provider-specific data as a specific type
     */
    @SuppressWarnings("unchecked")
    public <T> T getProviderSpecificDataAs(Class<T> clazz) {
        return (T) providerSpecificData;
    }
}

