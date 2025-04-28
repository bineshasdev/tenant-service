package com.offisync360.account.model;
 
import jakarta.persistence.Entity; 
import jakarta.persistence.Id;
import jakarta.persistence.Table; 
import lombok.Data;

@Data
@Entity
@Table(name = "tenant_config") 
public class TenantConfig {
  
    @Id
    private String id; // Matches Keycloak tenant ID
    private String serverUrl;
    private String issuerUri;
    private String jwkSetUri;
    private String apiClientId;
    private String apiClientSecret;
    private String uiClientId;
    private String uiClientSecret;
}
