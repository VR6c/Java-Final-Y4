package com.example.demo.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Programmatic Java Configuration for Caffeine Cache with advanced rules.
 * Defines custom eviction policies, time-to-live (TTL), idle expiration (TTI),
 * capacity sizing, eviction logging, and metrics recording per cache domain.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    // Cache Name Constants
    public static final String CACHE_PRODUCTS = "products";
    public static final String CACHE_STUDENTS = "students";
    public static final String CACHE_MAJORS = "majors";
    public static final String CACHE_SUBJECTS = "subjects";
    public static final String CACHE_TEACHERS = "teachers";
    public static final String CACHE_CUSTOMERS = "customers";
    public static final String CACHE_ORDERS = "orders";
    public static final String CACHE_USERS = "users";
    public static final String CACHE_USER_DETAILS = "userDetails";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // 1. Default fallback specification for any dynamically created cache
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(50)
                .maximumSize(500)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats()
                .removalListener(createRemovalListener("default")));
                
        // User Details: Queried on EVERY authenticated request via JwtAuthenticationFilter
        cacheManager.registerCustomCache(CACHE_USER_DETAILS, Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(1000)
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .recordStats()
                .removalListener(createRemovalListener(CACHE_USER_DETAILS))
                .build());

        // Users: Authenticated user profiles
        cacheManager.registerCustomCache(CACHE_USERS, Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(1000)
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .recordStats()
                .removalListener(createRemovalListener(CACHE_USERS))
                .build());

        log.info("Caffeine CacheManager configured with programmatic custom cache specifications and statistics recording.");
        return cacheManager;
    }

    private <K, V> RemovalListener<K, V> createRemovalListener(String cacheName) {
        return (key, value, cause) -> {
            if (log.isDebugEnabled()) {
                log.debug("Caffeine Cache [{}] eviction -> key: {}, cause: {}", cacheName, key, cause);
            }
        };
    }
}
