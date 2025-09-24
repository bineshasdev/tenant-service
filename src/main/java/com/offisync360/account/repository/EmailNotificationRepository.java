package com.offisync360.account.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.offisync360.account.model.EmailNotification;

@Repository
public interface EmailNotificationRepository extends JpaRepository<EmailNotification, UUID> {
    
    List<EmailNotification> findByTenantIdOrderByCreatedAtDesc(String tenantId);
    
    List<EmailNotification> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    List<EmailNotification> findByStatusOrderByCreatedAtDesc(String status);
    
    Page<EmailNotification> findByTenantIdOrderByCreatedAtDesc(String tenantId, Pageable pageable);
    
    @Query("SELECT en FROM EmailNotification en WHERE en.emailType = :emailType AND en.status = :status ORDER BY en.createdAt DESC")
    List<EmailNotification> findByEmailTypeAndStatus(@Param("emailType") String emailType, @Param("status") String status);
    
    @Query("SELECT COUNT(en) FROM EmailNotification en WHERE en.recipientEmail = :email AND en.createdAt >= :since")
    Long countEmailsSince(@Param("email") String email, @Param("since") LocalDateTime since);
}