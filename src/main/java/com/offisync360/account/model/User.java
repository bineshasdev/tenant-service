package com.offisync360.account.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
@Builder
public class User {
   
    @Id
    private String id; // Matches Keycloak user ID

    private String email;
    private String firstName;
    private String lastName; 
    private String phone; 
    private String country; 
    private String state; 
    private String pincode; 
    private LocalDateTime startDateEffective; 
    private LocalDateTime endDateEffective; 
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;
 
}