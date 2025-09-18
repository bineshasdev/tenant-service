package com.offisync360.account.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.offisync360.account.model.MobileVerification;

@Repository
public interface MobileVerificationRepository extends JpaRepository<MobileVerification, Long> {
    
    Optional<MobileVerification> findByPhoneNumberAndStatusAndExpiresAtAfter(
        String phoneNumber, String status, LocalDateTime now);
    
    List<MobileVerification> findByPhoneNumberOrderByCreatedAtDesc(String phoneNumber);
    
    List<MobileVerification> findByTenantIdAndStatus(String tenantId, String status);
    
    @Query("SELECT mv FROM MobileVerification mv WHERE mv.phoneNumber = :phoneNumber AND mv.status = 'PENDING' AND mv.expiresAt > :now ORDER BY mv.createdAt DESC")
    Optional<MobileVerification> findActiveVerification(@Param("phoneNumber") String phoneNumber, @Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(mv) FROM MobileVerification mv WHERE mv.phoneNumber = :phoneNumber AND mv.createdAt >= :since")
    Long countVerificationsSince(@Param("phoneNumber") String phoneNumber, @Param("since") LocalDateTime since);
}