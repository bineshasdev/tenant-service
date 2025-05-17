package com.offisync360.account.service;

import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class TenantIdService {

    private static final Pattern TENANT_ID_PATTERN = Pattern.compile("^[a-z0-9]{3,20}$");
    private static final int MAX_ATTEMPTS = 5;

    /**
     * Generates a tenant ID based on company name with fallback to UUID
     */
    public String generateTenantId(String companyName) {
        // First try: normalized company name
        String normalized = normalizeCompanyName(companyName);
        if (isValidTenantId(normalized)) {
            return normalized;
        }

        // Second try: normalized with suffix
        String withSuffix = normalized + "-" + generateRandomSuffix();
        if (isValidTenantId(withSuffix)) {
            return withSuffix;
        }

        // Fallback: UUID-based
        return generateUuidBasedTenantId();
    }

    /**
     * Normalizes company name to create tenant ID candidate
     */
    private String normalizeCompanyName(String companyName) {
        if (companyName == null) {
            return generateUuidBasedTenantId();
        }

        return companyName.trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "-")  // Replace special chars with hyphen
                .replaceAll("-+", "-")         // Replace multiple hyphens with single
                .replaceAll("^-|-$", "");      // Remove leading/trailing hyphens
    }

    /**
     * Validates tenant ID against pattern
     */
    public boolean isValidTenantId(String tenantId) {
        return tenantId != null 
                && TENANT_ID_PATTERN.matcher(tenantId).matches()
                && !isReservedId(tenantId);
    }

    /**
     * Checks against reserved IDs (like 'admin', 'system', etc.)
     */
    private boolean isReservedId(String tenantId) {
        return tenantId.matches("^(admin|system|keycloak|auth|api|www|support)$");
    }

    /**
     * Generates a random suffix (3 alphanumeric chars)
     */
    private String generateRandomSuffix() {
        return UUID.randomUUID().toString().substring(0, 3);
    }

    /**
     * Generates a UUID-based tenant ID (fallback)
     */
    private String generateUuidBasedTenantId() {
        return "t-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }

    /**
     * Ensures unique tenant ID by checking against existing ones
     */
    public String ensureUniqueTenantId(String proposedId, TenantExistenceChecker existenceChecker) {
        if (!isValidTenantId(proposedId)) {
            proposedId = generateUuidBasedTenantId();
        }

        String candidate = proposedId;
        int attempts = 0;

        while (existenceChecker.tenantExists(candidate) && attempts < MAX_ATTEMPTS) {
            candidate = proposedId + "-" + generateRandomSuffix();
            attempts++;
        }

        if (attempts >= MAX_ATTEMPTS) {
            return generateUuidBasedTenantId();
        }

        return candidate;
    }

    @FunctionalInterface
    public interface TenantExistenceChecker {
        boolean tenantExists(String tenantId);
    }
}