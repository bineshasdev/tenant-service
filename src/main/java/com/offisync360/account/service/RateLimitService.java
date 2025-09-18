package com.offisync360.account.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.offisync360.account.model.RateLimitEntry;
import com.offisync360.account.repository.RateLimitEntryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {
    
    private final RateLimitEntryRepository rateLimitEntryRepository;
    
    // Rate limit configurations
    private static final int DEFAULT_WINDOW_SIZE_MINUTES = 15;
    private static final int DEFAULT_MAX_REQUESTS = 100;
    
    // Specific endpoint rate limits
    private static final int SIGNUP_RATE_LIMIT = 5; // 5 signups per 15 minutes
    private static final int LOGIN_RATE_LIMIT = 10; // 10 login attempts per 15 minutes
    private static final int OTP_RATE_LIMIT = 3; // 3 OTP requests per 15 minutes
    private static final int API_RATE_LIMIT = 1000; // 1000 API calls per 15 minutes
    
    /**
     * Checks if request is within rate limit
     */
    @Transactional
    public boolean isAllowed(String identifier, String endpoint) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = now.minusMinutes(DEFAULT_WINDOW_SIZE_MINUTES);
        
        // Get or create rate limit entry
        Optional<RateLimitEntry> existingEntry = rateLimitEntryRepository
                .findActiveEntry(identifier, endpoint, now);
        
        RateLimitEntry entry;
        if (existingEntry.isPresent()) {
            entry = existingEntry.get();
            entry.setRequestCount(entry.getRequestCount() + 1);
            entry.setUpdatedAt(now);
        } else {
            entry = RateLimitEntry.builder()
                    .identifier(identifier)
                    .endpoint(endpoint)
                    .requestCount(1)
                    .windowStart(windowStart)
                    .windowEnd(now.plusMinutes(DEFAULT_WINDOW_SIZE_MINUTES))
                    .build();
        }
        
        rateLimitEntryRepository.save(entry);
        
        // Check rate limit
        int maxRequests = getMaxRequestsForEndpoint(endpoint);
        boolean allowed = entry.getRequestCount() <= maxRequests;
        
        if (!allowed) {
            log.warn("Rate limit exceeded for {} on endpoint {}: {}/{}", 
                    identifier, endpoint, entry.getRequestCount(), maxRequests);
        }
        
        return allowed;
    }
    
    /**
     * Gets remaining requests for an identifier and endpoint
     */
    public int getRemainingRequests(String identifier, String endpoint) {
        LocalDateTime now = LocalDateTime.now();
        Optional<RateLimitEntry> entry = rateLimitEntryRepository
                .findActiveEntry(identifier, endpoint, now);
        
        if (entry.isEmpty()) {
            return getMaxRequestsForEndpoint(endpoint);
        }
        
        int maxRequests = getMaxRequestsForEndpoint(endpoint);
        return Math.max(0, maxRequests - entry.get().getRequestCount());
    }
    
    /**
     * Gets rate limit info for an identifier and endpoint
     */
    public RateLimitInfo getRateLimitInfo(String identifier, String endpoint) {
        LocalDateTime now = LocalDateTime.now();
        Optional<RateLimitEntry> entry = rateLimitEntryRepository
                .findActiveEntry(identifier, endpoint, now);
        
        int maxRequests = getMaxRequestsForEndpoint(endpoint);
        int currentRequests = entry.map(RateLimitEntry::getRequestCount).orElse(0);
        int remainingRequests = Math.max(0, maxRequests - currentRequests);
        
        LocalDateTime resetTime = entry.map(RateLimitEntry::getWindowEnd).orElse(now.plusMinutes(DEFAULT_WINDOW_SIZE_MINUTES));
        
        return RateLimitInfo.builder()
                .limit(maxRequests)
                .remaining(remainingRequests)
                .resetTime(resetTime)
                .build();
    }
    
    /**
     * Cleans up expired rate limit entries
     */
    @Transactional
    public void cleanupExpiredEntries() {
        LocalDateTime expiredBefore = LocalDateTime.now().minusMinutes(DEFAULT_WINDOW_SIZE_MINUTES);
        rateLimitEntryRepository.deleteExpiredEntries(expiredBefore);
        log.debug("Cleaned up expired rate limit entries");
    }
    
    /**
     * Gets max requests for specific endpoint
     */
    private int getMaxRequestsForEndpoint(String endpoint) {
        if (endpoint.contains("/signup")) {
            return SIGNUP_RATE_LIMIT;
        } else if (endpoint.contains("/login")) {
            return LOGIN_RATE_LIMIT;
        } else if (endpoint.contains("/otp") || endpoint.contains("/verify")) {
            return OTP_RATE_LIMIT;
        } else {
            return API_RATE_LIMIT;
        }
    }
    
    /**
     * Rate limit info DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class RateLimitInfo {
        private int limit;
        private int remaining;
        private LocalDateTime resetTime;
    }
}