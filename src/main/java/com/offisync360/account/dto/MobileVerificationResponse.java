package com.offisync360.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileVerificationResponse {
    
    private boolean verified;
    private String message;
    private String phoneNumber;
    private Long expiresInSeconds;
}