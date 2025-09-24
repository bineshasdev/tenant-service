package com.offisync360.account.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.offisync360.account.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
     List<User> findByTenantId(String tenantId);
    
     long countByTenantId(String tenantId);

    // Alternative implementation with explicit JPQL
    @Query("SELECT COUNT(u) FROM User u WHERE u.tenant.id = :tenantId")
    long countUsersByTenant(@Param("tenantId") String tenantId);

    // Get all users for a tenant (pagination supported)
    Page<User> findByTenantId(String tenantId, Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM users WHERE tenant_id = :tenantId", 
       nativeQuery = true)
    long fastCountByTenant(@Param("tenantId") String tenantId);
    
    Optional<User> findByEmailAndTenantId(String email, String tenantId);
}