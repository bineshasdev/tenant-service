package com.offisync360.account.common;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.offisync360.account.model.SubscriptionPlan;
import com.offisync360.account.repository.SubscriptionPlanRepository;
 
import java.util.List;

@Component
@RequiredArgsConstructor
public class SubscriptionPlanInitializer {
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    @PostConstruct
    public void init() {
        
    }
 
}