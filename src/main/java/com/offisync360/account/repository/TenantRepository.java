package com.offisync360.account.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.offisync360.account.model.Tenant;
import com.offisync360.account.model.TenantConfig;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, String> {
    
    Optional<Tenant> findByDomain(String domain); 

    @Query("SELECT c FROM TenantConfig c WHERE c.id = ?1")
    Optional<TenantConfig> findConfigById(UUID id);

    boolean existsByDomain(String domain);

    boolean existsByAdminEmail(String email);

    boolean existsByRealmName(String realmName);
}