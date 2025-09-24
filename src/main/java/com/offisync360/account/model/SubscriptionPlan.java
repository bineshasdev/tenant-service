package com.offisync360.account.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data; 

@Entity
@Table(name = "subscription_plans")
@Data
@Builder
@AllArgsConstructor 
public class SubscriptionPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "max_users", nullable = false)
    private Integer maxUsers;

    @Column(name = "max_storage_gb")
    private Integer maxStorageGB;

    @Column(name = "price_monthly", precision = 10, scale = 2)
    private BigDecimal monthlyPrice;

    @Column(name = "price_yearly", precision = 10, scale = 2)
    private BigDecimal yearlyPrice;

    @Column(name = "features", columnDefinition = "jsonb")
    private String features;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; 

    public SubscriptionPlan() {}

    public SubscriptionPlan(UUID id, String name, Integer maxUsers, BigDecimal monthlyPrice) {
        this.id = id;
        this.name = name;
        this.maxUsers = maxUsers;
        this.monthlyPrice = monthlyPrice;
    }

    // Factory methods for different subscription plans
    
    public static SubscriptionPlan free() {
        return SubscriptionPlan.builder()
                .code("FREE")
                .name("Free Plan")
                .description("Basic plan with limited features")
                .maxUsers(5)
                .maxStorageGB(1)
                .monthlyPrice(BigDecimal.valueOf(0.00))
                .yearlyPrice(BigDecimal.valueOf(0.00))
                .features("{\"features\": [\"basic_support\", \"standard_storage\"]}")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static SubscriptionPlan basic() {
        return SubscriptionPlan.builder()
                .code("BASIC")
                .name("Basic Plan")
                .description("Standard plan for small teams")
                .maxUsers(25)
                .maxStorageGB(10)
                .monthlyPrice(BigDecimal.valueOf(29.99))
                .yearlyPrice(BigDecimal.valueOf(299.99))
                .features("{\"features\": [\"priority_support\", \"advanced_storage\", \"api_access\"]}")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static SubscriptionPlan professional() {
        return SubscriptionPlan.builder()
                .code("PROFESSIONAL")
                .name("Professional Plan")
                .description("Advanced plan for growing businesses")
                .maxUsers(100)
                .maxStorageGB(50)
                .monthlyPrice(BigDecimal.valueOf(99.99))
                .yearlyPrice(BigDecimal.valueOf(999.99))
                .features("{\"features\": [\"24x7_support\", \"unlimited_storage\", \"api_access\", \"custom_integrations\"]}")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static SubscriptionPlan enterprise() {
        return SubscriptionPlan.builder()
                .code("ENTERPRISE")
                .name("Enterprise Plan")
                .description("Full-featured plan for large organizations")
                .maxUsers(-1) // Unlimited users
                .maxStorageGB(-1) // Unlimited storage
                .monthlyPrice(BigDecimal.valueOf(299.99))
                .yearlyPrice(BigDecimal.valueOf(2999.99))
                .features("{\"features\": [\"dedicated_support\", \"unlimited_storage\", \"api_access\", \"custom_integrations\", \"sso\", \"audit_logs\"]}")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // Legacy method for backward compatibility
    public static SubscriptionPlan pro() {
        return professional();
    }
}