package com.offisync360.account.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantSettings {
    
    // Authentication Provider Settings
    private String authProvider; // KEYCLOAK, AWS_COGNITO, AUTH0, SPRING_AUTH_SERVER
    private String authServerUrl;
    private String realmName;
    private String clientId;
    private String clientSecret;
    
    // Token Settings
    private Integer accessTokenLifespan; // in seconds
    private Integer refreshTokenLifespan; // in seconds
    private Integer ssoSessionIdleTimeout; // in seconds
    private Integer ssoSessionMaxLifespan; // in seconds
    
    // User Registration Settings
    private Boolean allowEmailAsUsername;
    private Boolean emailVerificationRequired;
    private Boolean registrationAllowed;
    private Boolean rememberMe;
    
    // Password Policy Settings
    private Integer minimumPasswordLength;
    private Boolean requireUppercase;
    private Boolean requireLowercase;
    private Boolean requireNumbers;
    private Boolean requireSpecialChars;
    private Integer passwordHistoryCount;
    private Integer maxPasswordAge; // in days
    
    // Security Settings
    private Boolean bruteForceDetection;
    private Integer maxFailureWaitSeconds;
    private Integer waitIncrementSeconds;
    private Integer maxDeltaTimeSeconds;
    private Integer failureFactor;
    private Integer minQuickLoginWaitSeconds;
    private Integer maxQuickLoginWaitSeconds;
    
    // OTP Settings
    private Boolean otpEnabled;
    private String otpType; // TOTP, HOTP
    private Integer otpDigits;
    private Integer otpPeriod; // in seconds
    private Integer otpCounter;
    private Integer otpAlgorithm; // SHA1, SHA256, SHA512
    
    // Social Login Settings
    private Boolean googleLoginEnabled;
    private String googleClientId;
    private String googleClientSecret;
    
    private Boolean facebookLoginEnabled;
    private String facebookClientId;
    private String facebookClientSecret;
    
    private Boolean microsoftLoginEnabled;
    private String microsoftClientId;
    private String microsoftClientSecret;
    
    // Default values
    public static TenantSettings getDefaultSettings() {
        return TenantSettings.builder()
                .authProvider("KEYCLOAK")
                .accessTokenLifespan(300) // 5 minutes
                .refreshTokenLifespan(1800) // 30 minutes
                .ssoSessionIdleTimeout(1800) // 30 minutes
                .ssoSessionMaxLifespan(36000) // 10 hours
                .allowEmailAsUsername(true)
                .emailVerificationRequired(true)
                .registrationAllowed(false)
                .rememberMe(true)
                .minimumPasswordLength(8)
                .requireUppercase(true)
                .requireLowercase(true)
                .requireNumbers(true)
                .requireSpecialChars(false)
                .passwordHistoryCount(3)
                .maxPasswordAge(90)
                .bruteForceDetection(true)
                .maxFailureWaitSeconds(900) // 15 minutes
                .waitIncrementSeconds(60) // 1 minute
                .maxDeltaTimeSeconds(43200) // 12 hours
                .failureFactor(30)
                .minQuickLoginWaitSeconds(60)
                .maxQuickLoginWaitSeconds(900)
                .otpEnabled(false)
                .otpType("TOTP")
                .otpDigits(6)
                .otpPeriod(30)
                .otpCounter(0)
                .otpAlgorithm(1) // SHA1
                .googleLoginEnabled(false)
                .facebookLoginEnabled(false)
                .microsoftLoginEnabled(false)
                .build();
    }
}