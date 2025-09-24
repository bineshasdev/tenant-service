package com.offisync360.account.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.offisync360.account.annotation.RateLimited;
import com.offisync360.account.dto.MobileVerificationRequest;
import com.offisync360.account.dto.MobileVerificationResponse;
import com.offisync360.account.service.MobileValidationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
@Slf4j
public class MobileVerificationController {

    private final MobileValidationService mobileValidationService;
 
    
    public ResponseEntity<MobileVerificationResponse> verifyMobile(
            @Valid @RequestBody MobileVerificationRequest request) {
        
        try {
            boolean verified = mobileValidationService.verifyOTP(request.getPhoneNumber(), request.getOtpCode());
            
            MobileVerificationResponse response = MobileVerificationResponse.builder()
                    .verified(verified)
                    .message(verified ? "Mobile number verified successfully" : "Verification failed")
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Mobile verification failed for {}: {}", request.getPhoneNumber(), e.getMessage());
            
            MobileVerificationResponse response = MobileVerificationResponse.builder()
                    .verified(false)
                    .message("Verification failed: " + e.getMessage())
                    .build();
            
            return ResponseEntity.badRequest().body(response);
        }
    }

   
    @PostMapping("/resend-otp")
    public ResponseEntity<MobileVerificationResponse> resendOTP(
            @Valid @RequestBody MobileVerificationRequest request) {
        
        try {
            String otpCode = mobileValidationService.generateAndSendOTP(
                    request.getPhoneNumber(), 
                    request.getTenantId(), 
                    request.getUserId());
            
            MobileVerificationResponse response = MobileVerificationResponse.builder()
                    .verified(false)
                    .message("OTP sent successfully")
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to resend OTP for {}: {}", request.getPhoneNumber(), e.getMessage());
            
            MobileVerificationResponse response = MobileVerificationResponse.builder()
                    .verified(false)
                    .message("Failed to send OTP: " + e.getMessage())
                    .build();
            
            return ResponseEntity.badRequest().body(response);
        }
    }
}