package com.offisync360.account.common;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.offisync360.account.model.SubscriptionPlan;
import com.offisync360.account.repository.SubscriptionPlanRepository;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SubscriptionPlanInitializer {
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    @PostConstruct
    public void init() {
        if (subscriptionPlanRepository.count() == 0) {
          //  List<SubscriptionPlan> plans = List.of(
             //   createPlan("FREE", 5, "0.00", false, false, 1),
            //    createPlan("BASIC", 20, "9.99", false, false, 10),
             //   createPlan("PREMIUM", 100, "29.99", true, false, 50),
              //  createPlan("ENTERPRISE", 1000, "99.99", true, true, 500)
            //);
          //  subscriptionPlanRepository.saveAll(plans);
        }
    }

    private SubscriptionPlan createPlan(String name, int maxUsers, String price, 
                                      boolean analytics, boolean support, int storage) {
        return SubscriptionPlan.builder()
                .name(name)
                .maxUsers(maxUsers)
                .monthlyPrice(new BigDecimal(price))
                .hasAdvancedAnalytics(analytics)
                .hasPrioritySupport(support)
                .maxStorageGB(storage)
                .build();
    }
}