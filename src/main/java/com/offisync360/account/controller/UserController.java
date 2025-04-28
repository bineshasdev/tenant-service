package com.offisync360.account.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.offisync360.account.dto.UserRegistrationRequest;
import com.offisync360.account.model.User;
import com.offisync360.account.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('tenant-admin')")
    public ResponseEntity<User> registerUser(
            @RequestBody UserRegistrationRequest request,
            @RequestHeader("X-Tenant-ID") String tenantId) {
        User user = null;//userService.registerUser(request,
               // tenantId);
        return ResponseEntity.ok(user);
    }
}