package com.offisync360.account.common;

import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import com.offisync360.account.model.TenantConfig;
import com.offisync360.account.service.TenantConfiguration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * TenantJwtDecoder is a component that provides a way to decode JWT tokens for different tenants.
 * It uses a map to cache the JwtDecoder instances for each tenant, ensuring that the same decoder is reused for the same tenant.
 * 
 * This class is responsible for creating and managing the JwtDecoder instances based on the tenant's configuration.
 * It retrieves the configuration for each tenant from the TenantConfiguration service and creates a NimbusJwtDecoder with the appropriate JWK Set URI.
 * It also sets a custom JWT validator to validate the tokens based on the tenant's issuer URI and client ID.
 * 
 */

@Component
public class TenantJwtDecoder {

    private final Map<String, JwtDecoder> decoders = new ConcurrentHashMap<>();
    private final TenantConfiguration tenantConfiguration;

    public TenantJwtDecoder(TenantConfiguration tenantConfiguration) {
        this.tenantConfiguration = tenantConfiguration;
    }

    public JwtDecoder getJwtDecoder(String tenant) {
        return decoders.computeIfAbsent(tenant, t -> {
            TenantConfig config = tenantConfiguration.getConfig(t)
                .orElseThrow(() -> new RuntimeException("Unknown tenant: " + t));
            
            NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSetUri(config.getJwkSetUri())
                .build();
            
            decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(),
                new JwtIssuerValidator(config.getIssuerUri()),
                new JwtClaimValidator<String>("aud", aud -> aud.contains(config.getApiClientId()) || aud.contains(config.getUiClientId()) ))
            );
            
            return decoder;
        });
    }
}