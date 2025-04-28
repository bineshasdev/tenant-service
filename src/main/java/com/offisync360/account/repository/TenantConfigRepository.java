package com.offisync360.account.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.offisync360.account.model.TenantConfig;

public interface TenantConfigRepository extends JpaRepository<TenantConfig, String> {
  
     Optional<TenantConfig> findById(Long id);

}
