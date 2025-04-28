package com.offisync360.account.common;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;


/**
 * This class resolves the tenant identifier from the incoming request.
 * It checks for the tenant ID in the subdomain, headers, and query parameters.
 */

@Component
public class TenantResolver {

    public Optional<String> resolveTenant() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return Optional.empty();
        }
        HttpServletRequest request = attributes.getRequest();
        
        // 1. Check subdomain (e.g., tenant1.yourdomain.com)
        String host = request.getServerName();
        if (host.contains(".")) {
            String subdomain = host.split("\\.")[0];
            if (!subdomain.equals("www") && !subdomain.equals("api")) {
                return Optional.of(subdomain);
            }
        }
        
        // 2. Check header (X-Tenant-ID)
        String headerTenant = request.getHeader("X-Tenant-ID");
        if (headerTenant != null && !headerTenant.isBlank()) {
            return Optional.of(headerTenant);
        }
        
        // 3. Check query parameter (?tenant=tenant1)
        String queryTenant = request.getParameter("tenant");
        if (queryTenant != null && !queryTenant.isBlank()) {
            return Optional.of(queryTenant);
        }
        
        return Optional.empty();
    }
}