package com.offisync360.account.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.offisync360.account.model.SubscriptionPlan;
import com.offisync360.account.repository.SubscriptionPlanRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionPlanService {
 private final SubscriptionPlanRepository subscriptionPlanRepository;

    public List<SubscriptionPlan> getAllPlans() {
        return subscriptionPlanRepository.findAllByOrderByMonthlyPriceAsc();
    }

    public SubscriptionPlan createPlan(SubscriptionPlan plan) {
        return subscriptionPlanRepository.save(plan);
    }

    public Optional<SubscriptionPlan> updatePlan(String id, SubscriptionPlan planUpdates) {
        return subscriptionPlanRepository.findById(UUID.fromString(id)).map(existing -> { 
            if (planUpdates.getName() != null) existing.setName(planUpdates.getName());
            if (planUpdates.getMaxUsers() != null) existing.setMaxUsers(planUpdates.getMaxUsers());
            if (planUpdates.getMonthlyPrice() != null) existing.setMonthlyPrice(planUpdates.getMonthlyPrice());
            return subscriptionPlanRepository.save(existing);
        });
    }
}
