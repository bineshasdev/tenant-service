package com.offisync360.account.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic response for client/application creation across different authentication providers
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponse {
    
    private String clientId;
    private String clientName;
    private String clientSecret;
    private boolean confidential;
    private boolean enabled;
    private List<String> redirectUris;
    private List<String> webOrigins;
    private boolean directAccessGrantsEnabled;
    private boolean standardFlowEnabled;
    private boolean serviceAccountsEnabled;
    
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

