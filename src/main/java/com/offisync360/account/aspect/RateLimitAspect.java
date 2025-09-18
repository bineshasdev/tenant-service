package com.offisync360.account.aspect;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.offisync360.account.service.RateLimitService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitAspect {
    
    private final RateLimitService rateLimitService;
    
    @Around("@annotation(com.offisync360.account.annotation.RateLimited)")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // Get request context
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }
        
        HttpServletRequest request = attributes.getRequest();
        String identifier = getIdentifier(request);
        String endpoint = request.getRequestURI();
        
        // Check rate limit
        if (!rateLimitService.isAllowed(identifier, endpoint)) {
            log.warn("Rate limit exceeded for {} on {}", identifier, endpoint);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header("X-RateLimit-Limit", String.valueOf(rateLimitService.getRateLimitInfo(identifier, endpoint).getLimit()))
                    .header("X-RateLimit-Remaining", "0")
                    .body("Rate limit exceeded. Please try again later.");
        }
        
        // Proceed with the request
        Object result = joinPoint.proceed();
        
        // Add rate limit headers to response
        if (result instanceof ResponseEntity) {
            RateLimitService.RateLimitInfo rateLimitInfo = rateLimitService.getRateLimitInfo(identifier, endpoint);
            ResponseEntity<?> response = (ResponseEntity<?>) result;
            
            return ResponseEntity.status(response.getStatusCode())
                    .headers(response.getHeaders())
                    .header("X-RateLimit-Limit", String.valueOf(rateLimitInfo.getLimit()))
                    .header("X-RateLimit-Remaining", String.valueOf(rateLimitInfo.getRemaining()))
                    .header("X-RateLimit-Reset", String.valueOf(rateLimitInfo.getResetTime().toEpochSecond(java.time.ZoneOffset.UTC)))
                    .body(response.getBody());
        }
        
        return result;
    }
    
    /**
     * Gets identifier for rate limiting (IP address, user ID, or tenant ID)
     */
    private String getIdentifier(HttpServletRequest request) {
        // Try to get user ID from security context first
        String userId = getCurrentUserId();
        if (userId != null) {
            return "user:" + userId;
        }
        
        // Try to get tenant ID from request
        String tenantId = request.getHeader("X-Tenant-ID");
        if (tenantId != null) {
            return "tenant:" + tenantId;
        }
        
        // Fall back to IP address
        return "ip:" + getClientIpAddress(request);
    }
    
    /**
     * Gets current user ID from security context
     */
    private String getCurrentUserId() {
        // TODO: Implement based on your security setup
        // This would typically get the user ID from JWT token or security context
        return null;
    }
    
    /**
     * Gets client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}