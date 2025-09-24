# Multi-Authentication Provider System

This document describes the multi-authentication provider system implemented in the tenant service, allowing you to switch between different authentication providers like Keycloak, AWS Cognito, Auth0, and Spring Authorization Server.

## Architecture

The system uses a factory pattern with the following components:

1. **AuthenticationProvider Interface**: Defines common operations for all authentication providers
2. **AuthenticationProviderFactory**: Manages provider instances and selects the appropriate provider
3. **TenantSettings**: Configuration class for tenant-specific authentication settings
4. **Provider Implementations**: Concrete implementations for each authentication service

## Supported Providers

### 1. Keycloak (Default)
- **Provider Type**: `KEYCLOAK`
- **Implementation**: `KeycloakAuthenticationProvider`
- **Status**: ✅ Fully implemented

### 2. AWS Cognito
- **Provider Type**: `AWS_COGNITO`
- **Implementation**: `AwsCognitoAuthenticationProvider`
- **Status**: 🚧 Placeholder (not yet implemented)

### 3. Auth0
- **Provider Type**: `AUTH0`
- **Implementation**: `Auth0AuthenticationProvider`
- **Status**: 🚧 Placeholder (not yet implemented)

### 4. Spring Authorization Server
- **Provider Type**: `SPRING_AUTH_SERVER`
- **Implementation**: `SpringAuthServerAuthenticationProvider`
- **Status**: 🚧 Placeholder (not yet implemented)

## Configuration

### Application Properties

Configure the default authentication provider in `application.yml`:

```yaml
app:
  auth:
    provider: KEYCLOAK  # Options: KEYCLOAK, AWS_COGNITO, AUTH0, SPRING_AUTH_SERVER
  tenant:
    default-settings:
      access-token-lifespan: 300
      refresh-token-lifespan: 1800
      sso-session-idle-timeout: 1800
      sso-session-max-lifespan: 36000
      allow-email-as-username: true
      email-verification-required: true
      registration-allowed: false
      remember-me: true
      minimum-password-length: 8
      require-uppercase: true
      require-lowercase: true
      require-numbers: true
      require-special-chars: false
      password-history-count: 3
      max-password-age: 90
      brute-force-detection: true
      max-failure-wait-seconds: 900
      wait-increment-seconds: 60
      max-delta-time-seconds: 43200
      failure-factor: 30
      min-quick-login-wait-seconds: 60
      max-quick-login-wait-seconds: 900
      otp-enabled: false
      otp-type: TOTP
      otp-digits: 6
      otp-period: 30
      otp-counter: 0
      otp-algorithm: 1
      google-login-enabled: false
      facebook-login-enabled: false
      microsoft-login-enabled: false
```

### Environment Variables

You can override the authentication provider using environment variables:

```bash
export APP_AUTH_PROVIDER=AWS_COGNITO
```

## Usage

### In TenantService

The `TenantService` now uses the authentication provider factory to create realms, users, and clients:

```java
// Get authentication provider
AuthenticationProvider authProvider = authProviderFactory.getDefaultProvider();

// Create realm
RealmRepresentation realm = authProvider.createRealm(realmName, displayName, settings);

// Create admin user
UserRepresentation adminUser = authProvider.createAdminUser(
    realmName, email, password, firstName, lastName, settings);

// Create clients
ClientRepresentation apiClient = authProvider.createClient(
    realmName, clientId, clientName, true, settings);
```

### In SubscriptionEnforcementService

User count is retrieved using the configured authentication provider:

```java
AuthenticationProvider authProvider = authProviderFactory.getDefaultProvider();
long userCount = authProvider.getUserCount(realmName);
```

## Adding New Authentication Providers

To add a new authentication provider:

1. **Implement the AuthenticationProvider interface**:
   ```java
   @Component
   public class NewAuthProvider implements AuthenticationProvider {
       @Override
       public String getProviderType() {
           return "NEW_AUTH_PROVIDER";
       }
       
       // Implement other methods...
   }
   ```

2. **Add configuration**:
   ```yaml
   app:
     auth:
       provider: NEW_AUTH_PROVIDER
   ```

3. **The factory will automatically discover and register your provider**

## Tenant Settings

The `TenantSettings` class provides fine-grained control over authentication behavior:

- **Token Settings**: Lifespans for access and refresh tokens
- **User Registration**: Email verification, registration policies
- **Password Policy**: Complexity requirements, history, aging
- **Security Settings**: Brute force detection, failure handling
- **OTP Settings**: Two-factor authentication configuration
- **Social Login**: Google, Facebook, Microsoft integration

## Migration from Direct Keycloak Usage

If you're migrating from direct Keycloak usage:

1. **Replace direct KeycloakAdminClient calls** with `AuthenticationProvider` calls
2. **Update configuration** to use the new `app.auth.provider` setting
3. **Use TenantSettings** instead of hardcoded values
4. **Test with different providers** to ensure compatibility

## Benefits

- **Provider Agnostic**: Easy to switch between authentication services
- **Configurable**: Fine-grained control over authentication behavior
- **Extensible**: Simple to add new authentication providers
- **Maintainable**: Clean separation of concerns
- **Testable**: Easy to mock and unit test

## Future Enhancements

- Implement AWS Cognito provider
- Implement Auth0 provider
- Implement Spring Authorization Server provider
- Add database persistence for tenant settings
- Add runtime provider switching
- Add provider-specific configuration validation
