package com.offisync360.account.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.offisync360.account.model.UsageMetrics;

@Repository
public interface UsageMetricsRepository extends JpaRepository<UsageMetrics, UUID> {
    
    List<UsageMetrics> findByTenantIdOrderByMetricDateDesc(String tenantId);
    
    List<UsageMetrics> findByTenantIdAndMetricTypeOrderByMetricDateDesc(String tenantId, String metricType);
    
    Optional<UsageMetrics> findFirstByTenantIdAndMetricTypeOrderByMetricDateDesc(String tenantId, String metricType);
    
    @Query("SELECT um FROM UsageMetrics um WHERE um.tenant.id = :tenantId AND um.metricDate >= :startDate AND um.metricDate <= :endDate ORDER BY um.metricDate DESC")
    List<UsageMetrics> findByTenantIdAndDateRange(@Param("tenantId") String tenantId, 
                                                  @Param("startDate") LocalDateTime startDate, 
                                                  @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT um FROM UsageMetrics um WHERE um.tenant.id = :tenantId AND um.isOverLimit = true ORDER BY um.metricDate DESC")
    List<UsageMetrics> findOverLimitMetricsByTenantId(@Param("tenantId") String tenantId);
    
    @Query("SELECT COUNT(um) FROM UsageMetrics um WHERE um.tenant.id = :tenantId AND um.metricType = :metricType AND um.metricDate >= :startDate")
    Long countMetricsSince(@Param("tenantId") String tenantId, 
                          @Param("metricType") String metricType, 
                          @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT AVG(um.currentUsage) FROM UsageMetrics um WHERE um.tenant.id = :tenantId AND um.metricType = :metricType AND um.metricDate >= :startDate")
    Double getAverageUsageSince(@Param("tenantId") String tenantId, 
                               @Param("metricType") String metricType, 
                               @Param("startDate") LocalDateTime startDate);
   
    @Query("SELECT COUNT(um) FROM UsageMetrics um WHERE um.tenant.id = :tenantId AND um.metricType = :metricType")
    Long countMetricsByTenant(@Param("tenantId") String tenantId,  @Param("metricType") String metricType );               
}