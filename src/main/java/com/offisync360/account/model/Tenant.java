package com.offisync360.account.model;

import java.time.LocalDateTime; 
import java.util.UUID;
 
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id; 
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter; 

@Entity
@Table(name = "tenants"  )
@Getter
@Setter
@Builder
@NoArgsConstructor 
@AllArgsConstructor 

public class Tenant {

    @Id 
    private String id; 
    
    @Version
    private Long version = 0L;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "realm_name", nullable = false, unique = true)
    private String realmName;

    @Column(name = "domain")
    private String domain;

    @Column(name = "admin_email", nullable = false)
    private String adminEmail;

    @Column(name = "phone")
    private String phone;

    @Column(name = "country")
    private String country;

    @Column(name = "state")
    private String state;

    @Column(name = "pincode")
    private String pincode;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "start_date_effective")
    private LocalDateTime startDateEffective;

    @Column(name = "end_date_effective")
    private LocalDateTime endDateEffective;
     
    @Column(name = "locale")
    private String locale;

    @Column(name = "currency")
    private String currency;

    @Column(name = "admin_temp_password")
    private String adminTempPassword;
 
    @Column(name = "client_id")
    private String clientId;

    @Column(name = "client_secret")
    private String clientSecret;

    @Column(name = "public_client_id")
    private String publicClientId;

    @Column(name = "public_client_secret")
    private String publicClientSecret;

    @Column(name = "status")
    private String status;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
 
}
