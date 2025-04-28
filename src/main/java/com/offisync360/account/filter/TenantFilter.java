package com.offisync360.account.filter;

import java.io.IOException;

import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;

@Component
@WebFilter("/*")
public class TenantFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) 
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        String tenantId = request.getHeader("X-Tenant-ID");
        
        if (tenantId != null) {
            TenantContext.setCurrentTenant(tenantId);
        }
        
        chain.doFilter(req, res);
        TenantContext.clear();
    }
}