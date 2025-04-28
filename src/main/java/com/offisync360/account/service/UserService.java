package com.offisync360.account.service;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.offisync360.account.dto.UserRegistrationRequest;
import com.offisync360.account.exception.LocalizedException;
import com.offisync360.account.model.Tenant;
import com.offisync360.account.model.User;
import com.offisync360.account.repository.TenantRepository;
import com.offisync360.account.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService { 
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final KeycloakAdminClient keycloakAdmin;
    private final SubscriptionEnforcementService subscriptionService;
 
}
