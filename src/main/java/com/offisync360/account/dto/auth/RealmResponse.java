package com.offisync360.account.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic response for realm/tenant creation across different authentication providers
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RealmResponse {
    
    private String realmId;
    private String realmName;
    private String displayName;
    private boolean enabled;
    private String authServerUrl;
    
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

