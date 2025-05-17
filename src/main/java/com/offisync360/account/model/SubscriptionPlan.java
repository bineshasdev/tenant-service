package com.offisync360.account.model;

import java.math.BigDecimal;

import com.offisync360.account.common.constants.Subscriptions;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Integer maxUsers;

    @Column( precision = 10, scale = 2)
    private BigDecimal monthlyPrice;

    // Additional fields for feature flags
    private Boolean hasAdvancedAnalytics = false;
    private Boolean hasPrioritySupport = false;
    private Integer maxStorageGB = 1;

    public SubscriptionPlan() {}

    public SubscriptionPlan(Long id, String name, Integer maxUsers, BigDecimal monthlyPrice) {
        this.id = id;
        this.name = name;
        this.maxUsers = maxUsers;
        this.monthlyPrice = monthlyPrice;
    }

    public static SubscriptionPlan basic() {
        return SubscriptionPlan.builder().code(Subscriptions.BASIC.getCode())
        .name(Subscriptions.BASIC.getDisplayName())
        .maxUsers(Subscriptions.BASIC.getMaxUsers()).monthlyPrice(BigDecimal.valueOf(0)).build();
    }

    public static SubscriptionPlan free() {
        return SubscriptionPlan.builder().code(Subscriptions.FREE.getCode())
        .name(Subscriptions.FREE.getDisplayName())
        .maxUsers(Subscriptions.FREE.getMaxUsers()).monthlyPrice(BigDecimal.valueOf(0)).build();
    }

    public static SubscriptionPlan pro() {
        return SubscriptionPlan.builder().code(Subscriptions.PRO.getCode())
        .name(Subscriptions.PRO.getDisplayName())
        .maxUsers(Subscriptions.PRO.getMaxUsers()).monthlyPrice(BigDecimal.valueOf(0)).build();
    }
}