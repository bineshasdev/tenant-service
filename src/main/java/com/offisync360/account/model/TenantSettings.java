package com.offisync360.account.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;

@Data
@Entity
@Table(name = "tenant_settings")
@Builder
public class TenantSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    private String key;

    private String value;

    private String label;

    private String helpText;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
