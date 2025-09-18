package com.offisync360.account.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import com.offisync360.account.model.Subscription;

@Data
public class SubscriptionChangeRequest {
    
    @NotBlank(message = "Current plan is required")
    private String currentPlan;
    
    @NotBlank(message = "New plan is required")
    private String newPlan;
    
    private Subscription.BillingCycle billingCycle = Subscription.BillingCycle.MONTHLY;
    
    private String reason;
}