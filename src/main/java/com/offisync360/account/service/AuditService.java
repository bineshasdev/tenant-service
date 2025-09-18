package com.offisync360.account.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.offisync360.account.model.AuditLog;
import com.offisync360.account.repository.AuditLogRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {
    
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * Logs an audit event
     */
    @Transactional
    public void logAuditEvent(String entityType, String entityId, String action, 
                             Object oldValues, Object newValues, String changedBy, 
                             HttpServletRequest request) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .oldValues(convertToJson(oldValues))
                    .newValues(convertToJson(newValues))
                    .changedBy(changedBy)
                    .changedAt(LocalDateTime.now())
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(request != null ? request.getHeader("User-Agent") : null)
                    .build();
            
            auditLogRepository.save(auditLog);
            log.debug("Audit log created for {} {} on {}", entityType, entityId, action);
            
        } catch (Exception e) {
            log.error("Failed to create audit log: {}", e.getMessage(), e);
            // Don't throw exception to avoid breaking the main flow
        }
    }
    
    /**
     * Logs a simple audit event without request context
     */
    @Transactional
    public void logAuditEvent(String entityType, String entityId, String action, 
                             Object oldValues, Object newValues, String changedBy) {
        logAuditEvent(entityType, entityId, action, oldValues, newValues, changedBy, null);
    }
    
    /**
     * Gets audit history for an entity
     */
    public List<AuditLog> getAuditHistory(String entityType, String entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByChangedAtDesc(entityType, entityId);
    }
    
    /**
     * Gets paginated audit history for an entity
     */
    public Page<AuditLog> getAuditHistory(String entityType, String entityId, Pageable pageable) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByChangedAtDesc(entityType, entityId, pageable);
    }
    
    /**
     * Gets audit history for a user
     */
    public List<AuditLog> getAuditHistoryByUser(String changedBy) {
        return auditLogRepository.findByChangedByOrderByChangedAtDesc(changedBy);
    }
    
    /**
     * Gets audit history within a date range
     */
    public List<AuditLog> getAuditHistoryByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByChangedAtBetween(startDate, endDate);
    }
    
    /**
     * Gets audit history by entity type and action
     */
    public List<AuditLog> getAuditHistoryByTypeAndAction(String entityType, String action) {
        return auditLogRepository.findByEntityTypeAndAction(entityType, action);
    }
    
    /**
     * Converts object to JSON string
     */
    private String convertToJson(Object obj) {
        if (obj == null) {
            return null;
        }
        
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("Failed to convert object to JSON: {}", e.getMessage());
            return obj.toString();
        }
    }
    
    /**
     * Gets client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}