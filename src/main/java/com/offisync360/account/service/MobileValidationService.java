package com.offisync360.account.service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.offisync360.account.exception.BusinessValidationException;
import com.offisync360.account.model.MobileVerification;
import com.offisync360.account.repository.MobileVerificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MobileValidationService {
    
    private final MobileVerificationRepository mobileVerificationRepository;
    private final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
    
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 3;
    private static final int MAX_OTP_REQUESTS_PER_HOUR = 5;
    
    /**
     * Validates and formats a phone number
     */
    public String validateAndFormatPhoneNumber(String phoneNumber, String countryCode) {
        try {
            PhoneNumber number = phoneNumberUtil.parse(phoneNumber, countryCode);
            
            if (!phoneNumberUtil.isValidNumber(number)) {
                throw new BusinessValidationException("Invalid phone number format");
            }
            
            return phoneNumberUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            throw new BusinessValidationException("Invalid phone number: " + e.getMessage());
        }
    }
    
    /**
     * Generates and sends OTP for mobile verification
     */
    @Transactional
    public String generateAndSendOTP(String phoneNumber, String tenantId, String userId) {
        // Validate phone number format
        String formattedPhone = validateAndFormatPhoneNumber(phoneNumber, "IN");
        
        // Check rate limiting
        checkRateLimit(formattedPhone);
        
        // Invalidate any existing pending OTPs
        invalidateExistingOTPs(formattedPhone);
        
        // Generate new OTP
        String otpCode = generateOTP();
        
        // Create verification record
        MobileVerification verification = MobileVerification.builder()
                .phoneNumber(formattedPhone)
                .otpCode(otpCode)
                .tenantId(tenantId)
                .userId(userId)
                .status("PENDING")
                .attempts(0)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .build();
        
        mobileVerificationRepository.save(verification);
        
        // TODO: Send OTP via SMS service (Twilio, AWS SNS, etc.)
        log.info("OTP generated for phone {}: {}", formattedPhone, otpCode);
        
        return otpCode; // In production, don't return OTP in response
    }
    
    /**
     * Verifies OTP code
     */
    @Transactional
    public boolean verifyOTP(String phoneNumber, String otpCode) {
        String formattedPhone = validateAndFormatPhoneNumber(phoneNumber, "IN");
        
        MobileVerification verification = mobileVerificationRepository
                .findActiveVerification(formattedPhone, LocalDateTime.now())
                .orElseThrow(() -> new BusinessValidationException("No active OTP found for this phone number"));
        
        // Check if already verified
        if ("VERIFIED".equals(verification.getStatus())) {
            throw new BusinessValidationException("Phone number already verified");
        }
        
        // Check if expired
        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            verification.setStatus("EXPIRED");
            mobileVerificationRepository.save(verification);
            throw new BusinessValidationException("OTP has expired. Please request a new one.");
        }
        
        // Check attempts
        if (verification.getAttempts() >= MAX_ATTEMPTS) {
            verification.setStatus("FAILED");
            mobileVerificationRepository.save(verification);
            throw new BusinessValidationException("Maximum verification attempts exceeded");
        }
        
        // Increment attempts
        verification.setAttempts(verification.getAttempts() + 1);
        
        // Verify OTP
        if (otpCode.equals(verification.getOtpCode())) {
            verification.setStatus("VERIFIED");
            verification.setVerifiedAt(LocalDateTime.now());
            mobileVerificationRepository.save(verification);
            return true;
        } else {
            mobileVerificationRepository.save(verification);
            throw new BusinessValidationException("Invalid OTP code");
        }
    }
    
    /**
     * Checks if phone number is verified
     */
    public boolean isPhoneVerified(String phoneNumber) {
        String formattedPhone = validateAndFormatPhoneNumber(phoneNumber, "IN");
        
        return mobileVerificationRepository
                .findByPhoneNumberAndStatusAndExpiresAtAfter(formattedPhone, "VERIFIED", LocalDateTime.now().minusDays(30))
                .isPresent();
    }
    
    /**
     * Generates a random OTP
     */
    private String generateOTP() {
        Random random = ThreadLocalRandom.current();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
    
    /**
     * Checks rate limiting for OTP requests
     */
    private void checkRateLimit(String phoneNumber) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        Long requestCount = mobileVerificationRepository.countVerificationsSince(phoneNumber, oneHourAgo);
        
        if (requestCount >= MAX_OTP_REQUESTS_PER_HOUR) {
            throw new BusinessValidationException("Too many OTP requests. Please try again later.");
        }
    }
    
    /**
     * Invalidates existing pending OTPs
     */
    private void invalidateExistingOTPs(String phoneNumber) {
        mobileVerificationRepository
                .findByPhoneNumberAndStatusAndExpiresAtAfter(phoneNumber, "PENDING", LocalDateTime.now())
                .ifPresent(verification -> {
                    verification.setStatus("EXPIRED");
                    mobileVerificationRepository.save(verification);
                });
    }
}