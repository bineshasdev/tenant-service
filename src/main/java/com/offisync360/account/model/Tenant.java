package com.offisync360.account.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity; 
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany; 
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor; 

@Entity
@Table(name = "tenants"  )
@Data
@Builder
@NoArgsConstructor 
@AllArgsConstructor 
public class Tenant {
    @Id
    private String id; // Matches Keycloak tenant ID
   
    private String displayName;

    private String realmName;

    @Column(unique = true)
    private String domain;

    private String adminEmail;

    private String phone;

    private String country;

    private String state;

    private String pincode;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime startDateEffective;

    private LocalDateTime endDateEffective;

    private String locale;

    private String currency;

    private String adminTempPassword;
 

}
