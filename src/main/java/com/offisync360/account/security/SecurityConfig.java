package com.offisync360.account.security;
import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.offisync360.account.common.TenantJwtDecoder;
import com.offisync360.account.common.TenantResolver;

import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
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
        .headers(headers -> headers
        .frameOptions(frameOptions -> frameOptions.deny())
        .contentTypeOptions(contentTypeOptions -> {})
        .httpStrictTransportSecurity(hstsConfig -> hstsConfig
            .maxAgeInSeconds(31536000)
            .includeSubDomains(true)
        )
        .referrerPolicy(referrerPolicy -> referrerPolicy.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
    )
        .authorizeHttpRequests(auth -> {
          /* auth
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/swagger-resources/**", "/swagger-resources", "/api-docs/**",
                    "/v3/**",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll();
            // Permit Swagger UI only for dev/local
          /*if (isDevOrLocal()) {
                auth
                    .requestMatchers(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                    ).permitAll();
            }
 */  
            auth 
            .requestMatchers("/api/v1/account/signup").permitAll()
            .requestMatchers("/api/v1/account/resolve-tenant").permitAll()
            .requestMatchers("/api/account/verify-mobile").permitAll()
            .requestMatchers("/api/account/resend-otp").permitAll()
            .requestMatchers("/actuator/health").permitAll()
            .requestMatchers("/actuator/info").permitAll()
            .requestMatchers("/swagger-ui/**").permitAll()
            .requestMatchers("/v3/api-docs/**").permitAll()
            .requestMatchers("/swagger-ui.html").permitAll()
            
            // Admin endpoints
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            
            // All other endpoints require authentication
            .anyRequest().authenticated();
        })
             
            .oauth2ResourceServer(oauth2 -> oauth2
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler())
                .jwt(Customizer.withDefaults())
              /* .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                ) */ 
            ) .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable());
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    private boolean isDevOrLocal() {
        String activeProfile = System.getProperty("spring.profiles.active", "");
        return activeProfile.contains("dev") || activeProfile.contains("local");
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