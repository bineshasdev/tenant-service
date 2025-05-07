package com.offisync360.account.security;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler; 

import com.offisync360.account.common.TenantJwtDecoder;
import com.offisync360.account.common.TenantResolver;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final TenantResolver tenantResolver;
    private final TenantJwtDecoder tenantJwtDecoder;

    public SecurityConfig(TenantResolver tenantResolver, TenantJwtDecoder tenantJwtDecoder) {
        this.tenantResolver = tenantResolver;
        this.tenantJwtDecoder = tenantJwtDecoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/account/signup").hasRole("account-admin")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler())
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            ) .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .csrf(csrf -> csrf.disable());
        return http.build();
    }

    /**
     * This method configures the JWT decoder to use a tenant-specific decoder based on the resolved tenant.
     * It retrieves the tenant ID from the request and uses it to get the appropriate JwtDecoder instance.
     * 
     * @return a JwtDecoder that resolves tenant-specific JWT decoders
     */

    private JwtDecoder jwtDecoder() {
        return token -> {
            String tenant = tenantResolver.resolveTenant()
                .orElseThrow(() -> new RuntimeException("Tenant not resolved"));
            return tenantJwtDecoder.getJwtDecoder(tenant).decode(token);
        };
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return converter;
    }

    private AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getOutputStream().println(
                String.format("{\"error\": \"Unauthorized\", \"message\": \"%s\"}", 
                authException.getMessage())
            );
        };
    }

    private AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getOutputStream().println(
                String.format("{\"error\": \"Forbidden\", \"message\": \"%s\"}", 
                accessDeniedException.getMessage())
            );
        };
    }
}