package com.offisync360.account.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.offisync360.account.model.Subscription;
import com.offisync360.account.model.Tenant;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    
    Optional<Subscription> findByTenantAndStatus(Tenant tenant, Subscription.SubscriptionStatus status);
    
    Optional<Subscription> findByTenantIdAndStatus(String tenantId, Subscription.SubscriptionStatus status);
    
    List<Subscription> findByStatusAndNextBillingDateBefore(Subscription.SubscriptionStatus status, LocalDateTime date);
    
    List<Subscription> findByStatusAndEndDateBefore(Subscription.SubscriptionStatus status, LocalDateTime date);
    
    @Query("SELECT s FROM Subscription s WHERE s.tenant.id = :tenantId AND s.status IN ('ACTIVE', 'TRIAL')")
    Optional<Subscription> findActiveSubscriptionByTenantId(@Param("tenantId") String tenantId);
    
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.status = :status")
    Long countByStatus(@Param("status") Subscription.SubscriptionStatus status);
    
    @Query("SELECT s FROM Subscription s WHERE s.trialEndDate <= :date AND s.status = 'TRIAL'")
    List<Subscription> findExpiredTrials(@Param("date") LocalDateTime date);
}