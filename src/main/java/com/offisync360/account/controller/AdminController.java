package com.offisync360.account.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.offisync360.account.model.AuditLog;
import com.offisync360.account.model.EmailNotification;
import com.offisync360.account.service.AuditService;
import com.offisync360.account.service.EmailNotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AuditService auditService;
    private final EmailNotificationService emailNotificationService;

    /**
     * Get audit logs for a specific entity
     */
    @GetMapping("/audit/{entityType}/{entityId}")
    public ResponseEntity<List<AuditLog>> getAuditLogs(
            @PathVariable String entityType,
            @PathVariable String entityId) {
        
        List<AuditLog> auditLogs = auditService.getAuditHistory(entityType, entityId);
        return ResponseEntity.ok(auditLogs);
    }

    /**
     * Get paginated audit logs for a specific entity
     */
    @GetMapping("/audit/{entityType}/{entityId}/page")
    public ResponseEntity<Page<AuditLog>> getAuditLogsPage(
            @PathVariable String entityType,
            @PathVariable String entityId,
            Pageable pageable) {
        
        Page<AuditLog> auditLogs = auditService.getAuditHistory(entityType, entityId, pageable);
        return ResponseEntity.ok(auditLogs);
    }

    /**
     * Get audit logs by user
     */
    @GetMapping("/audit/user/{userId}")
    public ResponseEntity<List<AuditLog>> getAuditLogsByUser(@PathVariable String userId) {
        List<AuditLog> auditLogs = auditService.getAuditHistoryByUser(userId);
        return ResponseEntity.ok(auditLogs);
    }

    /**
     * Get audit logs by date range
     */
    @GetMapping("/audit/date-range")
    public ResponseEntity<List<AuditLog>> getAuditLogsByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        // TODO: Parse dates and call auditService.getAuditHistoryByDateRange()
        return ResponseEntity.ok(List.of());
    }

    /**
     * Get email notification history for a tenant
     */
    @GetMapping("/emails/tenant/{tenantId}")
    public ResponseEntity<List<EmailNotification>> getEmailHistory(@PathVariable String tenantId) {
        List<EmailNotification> emails = emailNotificationService.getEmailHistory(tenantId);
        return ResponseEntity.ok(emails);
    }

    /**
     * Get failed email notifications
     */
    @GetMapping("/emails/failed")
    public ResponseEntity<List<EmailNotification>> getFailedEmails() {
        List<EmailNotification> failedEmails = emailNotificationService.getFailedEmails();
        return ResponseEntity.ok(failedEmails);
    }

    /**
     * Retry a failed email
     */
    @PostMapping("/emails/{emailId}/retry")
    public ResponseEntity<String> retryFailedEmail(@PathVariable Long emailId) {
        try {
            emailNotificationService.retryFailedEmail(emailId);
            return ResponseEntity.ok("Email retry initiated");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to retry email: " + e.getMessage());
        }
    }
}