package com.offisync360.account.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "email_notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailNotification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tenant_id")
    private String tenantId;
    
    @Column(name = "user_id")
    private String userId;
    
    @Column(name = "email_type", nullable = false)
    private String emailType; // SIGNUP_STARTED, SIGNUP_COMPLETED, WELCOME, etc.
    
    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;
    
    @Column(nullable = false)
    private String subject;
    
    @Column(name = "template_name")
    private String templateName;
    
    @Column(nullable = false)
    @Builder.Default
    private String status = "PENDING"; // PENDING, SENT, FAILED, BOUNCED
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;
    
    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}