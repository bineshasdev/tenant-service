package com.offisync360.account.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.offisync360.account.model.EmailNotification;
import com.offisync360.account.model.Tenant;
import com.offisync360.account.repository.EmailNotificationRepository;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final EmailNotificationRepository emailNotificationRepository;
    
    @Value("${app.email.from:noreply@offisync360.com}")
    private String fromEmail;
    
    @Value("${app.email.from-name:OffiSync360}")
    private String fromName;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    /**
     * Sends signup started notification
     */
    @Transactional
    public void sendSignupStartedNotification(Tenant tenant) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("tenantName", tenant.getDisplayName());
        variables.put("adminEmail", tenant.getAdminEmail());
        variables.put("companyName", tenant.getId());
        variables.put("baseUrl", baseUrl);
        
        sendEmail(
            tenant.getId().toString(),
            null,
            tenant.getAdminEmail(),
            "SIGNUP_STARTED",
            "Welcome to OffiSync360 - Account Setup in Progress",
            "signup-started",
            variables
        );
    }
    
    /**
     * Sends signup completed notification
     */
    @Transactional
    public void sendSignupCompletedNotification(Tenant tenant) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("tenantName", tenant.getDisplayName());
        variables.put("adminEmail", tenant.getAdminEmail());
        variables.put("loginUrl", baseUrl + "/login");
        variables.put("tempPassword", tenant.getAdminTempPassword());
        variables.put("baseUrl", baseUrl);
        
        sendEmail(
            tenant.getId().toString(),
            null,
            tenant.getAdminEmail(),
            "SIGNUP_COMPLETED",
            "Your OffiSync360 Account is Ready!",
            "signup-completed",
            variables
        );
    }
    
    /**
     * Sends welcome email to new users
     */
    @Transactional
    public void sendWelcomeEmail(String tenantId, String userId, String userEmail, String userName) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", userName);
        variables.put("userEmail", userEmail);
        variables.put("loginUrl", baseUrl + "/login");
        variables.put("baseUrl", baseUrl);
        
        sendEmail(
            tenantId,
            userId,
            userEmail,
            "WELCOME",
            "Welcome to OffiSync360!",
            "welcome",
            variables
        );
    }
    
    /**
     * Sends password reset email
     */
    @Transactional
    public void sendPasswordResetEmail(String tenantId, String userEmail, String resetToken) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("userEmail", userEmail);
        variables.put("resetUrl", baseUrl + "/reset-password?token=" + resetToken);
        variables.put("baseUrl", baseUrl);
        
        sendEmail(
            tenantId,
            null,
            userEmail,
            "PASSWORD_RESET",
            "Reset Your OffiSync360 Password",
            "password-reset",
            variables
        );
    }
    
    /**
     * Sends mobile verification OTP email (backup method)
     */
    @Transactional
    public void sendMobileVerificationOTP(String tenantId, String userEmail, String otpCode) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("userEmail", userEmail);
        variables.put("otpCode", otpCode);
        variables.put("baseUrl", baseUrl);
        
        sendEmail(
            tenantId,
            null,
            userEmail,
            "MOBILE_VERIFICATION",
            "Your Mobile Verification Code",
            "mobile-verification",
            variables
        );
    }
    
    /**
     * Generic email sending method
     */
    @Transactional
    public void sendEmail(String tenantId, String userId, String recipientEmail, 
                         String emailType, String subject, String templateName, 
                         Map<String, Object> variables) {
        
        try {
            // Create email notification record
            EmailNotification notification = EmailNotification.builder()
                    .tenantId(tenantId)
                    .userId(userId != null ? UUID.fromString(userId) : null)
                    .emailType(emailType)
                    .recipientEmail(recipientEmail)
                    .subject(subject)
                    .templateName(templateName)
                    .status("PENDING")
                    .build();
            
            emailNotificationRepository.save(notification);
            
            // Prepare email content
            Context context = new Context();
            context.setVariables(variables);
            
            String htmlContent = templateEngine.process("emails/" + templateName, context);
            
            // Create and send email
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(recipientEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(mimeMessage);
            
            // Update notification status
            notification.setStatus("SENT");
            notification.setSentAt(LocalDateTime.now());
            emailNotificationRepository.save(notification);
            
            log.info("Email sent successfully to {} with type {}", recipientEmail, emailType);
            
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Failed to send email to {}: {}", recipientEmail, e.getMessage());
            
            // Update notification status - find the most recent notification for this tenant
            List<EmailNotification> notifications = emailNotificationRepository
                    .findByTenantIdOrderByCreatedAtDesc(tenantId);
            
            if (!notifications.isEmpty()) {
                EmailNotification notification = notifications.get(0);
                notification.setStatus("FAILED");
                notification.setErrorMessage(e.getMessage());
                emailNotificationRepository.save(notification);
            }
            
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gets email notification history for a tenant
     */
    public List<EmailNotification> getEmailHistory(String tenantId) {
        return emailNotificationRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }
    
    /**
     * Gets failed email notifications for retry
     */
    public List<EmailNotification> getFailedEmails() {
        return emailNotificationRepository.findByStatusOrderByCreatedAtDesc("FAILED");
    }
    
    /**
     * Retries sending a failed email
     */
    @Transactional
    public void retryFailedEmail(UUID notificationId) {
        EmailNotification notification = emailNotificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Email notification not found"));
        
        if (!"FAILED".equals(notification.getStatus())) {
            throw new RuntimeException("Can only retry failed emails");
        }
        
        // Reset status and retry
        notification.setStatus("PENDING");
        notification.setErrorMessage(null);
        emailNotificationRepository.save(notification);
        
        // TODO: Implement retry logic
        log.info("Retrying email notification ID: {}", notificationId);
    }
}