package com.offisync360.account.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.offisync360.account.model.AuditLog;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    List<AuditLog> findByEntityTypeAndEntityIdOrderByChangedAtDesc(String entityType, String entityId);
    
    Page<AuditLog> findByEntityTypeAndEntityIdOrderByChangedAtDesc(String entityType, String entityId, Pageable pageable);
    
    List<AuditLog> findByChangedByOrderByChangedAtDesc(String changedBy);
    
    @Query("SELECT a FROM AuditLog a WHERE a.changedAt BETWEEN :startDate AND :endDate ORDER BY a.changedAt DESC")
    List<AuditLog> findByChangedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM AuditLog a WHERE a.entityType = :entityType AND a.action = :action ORDER BY a.changedAt DESC")
    List<AuditLog> findByEntityTypeAndAction(@Param("entityType") String entityType, @Param("action") String action);
}