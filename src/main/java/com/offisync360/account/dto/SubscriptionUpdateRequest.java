package com.offisync360.account.dto;

import java.util.UUID;
 

import lombok.Data;

@Data
public class SubscriptionUpdateRequest {

    private UUID newPlan;
    private String subscriptionCode;
}
