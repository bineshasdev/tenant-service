package com.offisync360.account.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods that should be rate limited
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {
    
    /**
     * Custom rate limit key (optional)
     * If not specified, will use endpoint path
     */
    String value() default "";
    
    /**
     * Rate limit window size in minutes
     */
    int windowSizeMinutes() default 15;
    
    /**
     * Maximum requests per window
     */
    int maxRequests() default 100;
}