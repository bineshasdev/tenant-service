package com.offisync360.account.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource; 
import org.keycloak.representations.idm.ClientRepresentation; 
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KeycloakAdminClient { 

    @Value("${keycloak.auth-server-url}")
    private String serverUrl;
    @Value("${keycloak.master-realm}")
    private String masterRealm;
    @Value("${keycloak.admin-client-id}")
    private String clientId;
    @Value("${keycloak.admin-client-secret}")
    private String clientSecret;
    @Value("${keycloak.admin-username}")
    private String username;
    @Value("${keycloak.admin-password}")
    private String password;


    public RealmRepresentation createRealm(String realmName, String displayName) {
        Keycloak keycloak = getAdminKeycloakInstance();
        
        RealmRepresentation realm = new RealmRepresentation();
        realm.setRealm(realmName);
        realm.setDisplayName(displayName);
        realm.setEnabled(true);
        realm.setRegistrationAllowed(false);
        realm.setRememberMe(true);
        
        keycloak.realms().create(realm);
        return realm;
    }

    public UserRepresentation createUser(String realmName, String email, 
                                          String password, String firstName, String lastName,
                                           boolean resetPassword, String role) {
        Keycloak keycloak = getAdminKeycloakInstance();
        
        // Create user
        UserRepresentation user = new UserRepresentation();
        user.setUsername(email);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);
        user.setEmailVerified(true);
        
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(resetPassword);
        
        user.setCredentials(List.of(credential));
        
        Response response = keycloak.realm(realmName).users().create(user);
        String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
        
        // Assign admin role
        if (role.equals("admin")) {

          RoleRepresentation adminRole = keycloak.realm(realmName).roles().get("admin").toRepresentation();
          keycloak.realm(realmName).users().get(userId).roles().realmLevel().add(List.of(adminRole));
        } else {
            RoleRepresentation userRole = keycloak.realm(realmName).roles().get("user").toRepresentation();
            keycloak.realm(realmName).users().get(userId).roles().realmLevel().add(List.of(userRole));
         
        }
        return user;
    }

    public ClientRepresentation createClient(String realmName, String clientId, 
                                           String clientName, boolean confidential) {
        Keycloak keycloak = getAdminKeycloakInstance();
        
        ClientRepresentation client = new ClientRepresentation();
        client.setClientId(clientId);
        client.setName(clientName);
        client.setConsentRequired(false);
        client.setProtocol("openid-connect");
        client.setPublicClient(!confidential);
        client.setDirectAccessGrantsEnabled(true);
        client.setStandardFlowEnabled(true);
        client.setServiceAccountsEnabled(true);
        if (confidential) {
            client.setSecret(UUID.randomUUID().toString());
        }
        
        // Default configuration
        client.setRedirectUris(List.of("*"));
        client.setWebOrigins(List.of("*"));
        client.setAttributes(Map.of(
            "post.logout.redirect.uris", "+",
            "exclude.session.state.from.auth.response", "false"
        ));
        
        keycloak.realm(realmName).clients().create(client);
        return client;
    }

    public void createRealmRoles(String realmName, List<String> roleNames) {
        Keycloak keycloak = getAdminKeycloakInstance();
        
        roleNames.forEach(roleName -> {
            RoleRepresentation role = new RoleRepresentation();
            role.setName(roleName);
            keycloak.realm(realmName).roles().create(role);
        });
    }

    private Keycloak getAdminKeycloakInstance() {
        return KeycloakBuilder.builder()
            .serverUrl(serverUrl) 
            .realm(masterRealm)
            .clientId(clientId)
            .username(username)
            .password(password)
            .build();
    }
 
    public String getServerUrl() {
        return serverUrl;
    }

    public boolean realmExists(String realm) {
        Keycloak keycloak = getAdminKeycloakInstance();
         try {
            RealmResource realmResource = keycloak.realm(realm);
            
            realmResource.toRepresentation();
            return true;
        } catch (Exception e) {
            log.error("Realm check error", e);
            return false;
        }
      
    }

    public int getUserCount(String realmName) {
        Keycloak keycloak = getAdminKeycloakInstance();
        try {
            return keycloak.realm(realmName).users().count();
        } catch (Exception e) {
            log.error("Error getting user count for realm {}: {}", realmName, e.getMessage());
            return 0;
        }
    }
}