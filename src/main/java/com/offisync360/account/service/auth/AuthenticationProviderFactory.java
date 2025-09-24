package com.offisync360.account.service.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class AuthenticationProviderFactory {
    
    @Value("${app.auth.provider:KEYCLOAK}")
    private String defaultAuthProvider;
    
    private final Map<String, AuthenticationProvider> providers;
    
    public AuthenticationProviderFactory(List<AuthenticationProvider> providerList) {
        this.providers = providerList.stream()
                .collect(java.util.stream.Collectors.toMap(
                    AuthenticationProvider::getProviderType,
                    provider -> provider
                ));
    }
    
    public AuthenticationProvider getProvider(String providerType) {
        String provider = providerType != null ? providerType : defaultAuthProvider;
        return providers.getOrDefault(provider, providers.get(defaultAuthProvider));
    }
    
    public AuthenticationProvider getDefaultProvider() {
        return providers.get(defaultAuthProvider);
    }
}
