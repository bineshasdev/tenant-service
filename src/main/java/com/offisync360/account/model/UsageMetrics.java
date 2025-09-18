package com.offisync360.account.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usage_metrics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageMetrics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;
    
    @Column(name = "metric_date", nullable = false)
    private LocalDateTime metricDate;
    
    @Column(name = "metric_type", nullable = false)
    private String metricType; // USERS, STORAGE, API_CALLS, etc.
    
    @Column(name = "current_usage", nullable = false)
    private Long currentUsage;
    
    @Column(name = "limit_value")
    private Long limitValue;
    
    @Column(name = "percentage_used")
    private Double percentageUsed;
    
    @Column(name = "is_over_limit", nullable = false)
    @Builder.Default
    private Boolean isOverLimit = false;
    
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    public enum MetricType {
        USERS("USERS"),
        STORAGE_GB("STORAGE_GB"),
        API_CALLS("API_CALLS"),
        EMAILS_SENT("EMAILS_SENT"),
        MOBILE_VERIFICATIONS("MOBILE_VERIFICATIONS");
        
        private final String value;
        
        MetricType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
}