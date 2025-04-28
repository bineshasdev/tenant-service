package com.offisync360.account.dto;

import com.offisync360.account.model.SubscriptionPlan;

import lombok.Data;

@Data
public class SubscriptionUpdateRequest {

    private SubscriptionPlan newPlan;
    private String subscriptionCode;
}
