package com.offisync360.account.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.offisync360.account.model.SubscriptionPlan;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    // Find plan by name
    Optional<SubscriptionPlan> findByName(String name);
    Optional<SubscriptionPlan> findByCode(String code);

    // Find the cheapest plan that can accommodate at least X users
    @Query("SELECT p FROM SubscriptionPlan p WHERE p.maxUsers >= ?1 ORDER BY p.monthlyPrice ASC")
    List<SubscriptionPlan> findPlansByMinUsers(long minUsers);

    // Find the first plan that can accommodate at least X users, ordered by price
    default Optional<SubscriptionPlan> findFirstByMaxUsersGreaterThanEqualOrderByMonthlyPriceAsc(long minUsers) {
        return findPlansByMinUsers(minUsers).stream().findFirst();
    }

    // Find all plans ordered by price ascending
    List<SubscriptionPlan> findAllByOrderByMonthlyPriceAsc();
}