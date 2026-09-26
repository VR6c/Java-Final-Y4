package com.example.demo.controller;

import com.example.demo.constant.AppConstants;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.CacheStatResponse;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/cache")
@Tag(name = "Cache Management", description = "Endpoints for inspecting and managing Caffeine caches")
public class CacheManagementController {

    private final CacheManager cacheManager;

    public CacheManagementController(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @GetMapping("/names")
    @Operation(summary = "Get all configured cache names")
    public ResponseEntity<ApiResponse<Collection<String>>> getCacheNames() {
        return ResponseEntity.ok(ApiResponse.success(cacheManager.getCacheNames(), AppConstants.SUCCESS_RETRIEVE));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get Caffeine performance metrics and stats for all caches")
    public ResponseEntity<ApiResponse<List<CacheStatResponse>>> getAllStats() {
        List<CacheStatResponse> statsList = new ArrayList<>();

        for (String cacheName : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache instanceof CaffeineCache caffeineCache) {
                com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();
                CacheStats stats = nativeCache.stats();

                double hitRate = stats.requestCount() > 0
                        ? Math.round(stats.hitRate() * 10000.0) / 100.0
                        : 0.0;

                statsList.add(CacheStatResponse.builder()
                        .cacheName(cacheName)
                        .estimatedSize(nativeCache.estimatedSize())
                        .requestCount(stats.requestCount())
                        .hitCount(stats.hitCount())
                        .missCount(stats.missCount())
                        .hitRatePercentage(hitRate)
                        .evictionCount(stats.evictionCount())
                        .build());
            }
        }

        return ResponseEntity.ok(ApiResponse.success(statsList, AppConstants.SUCCESS_RETRIEVE));
    }

    @PostMapping("/clear/{cacheName}")
    @Operation(summary = "Evict/clear all entries from a specific cache")
    public ResponseEntity<ApiResponse<String>> clearCache(@PathVariable String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            return ResponseEntity.ok(ApiResponse.success(cacheName, AppConstants.SUCCESS_DELETE));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/clear-all")
    @Operation(summary = "Evict/clear all entries from all caches")
    public ResponseEntity<ApiResponse<String>> clearAllCaches() {
        for (String cacheName : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        }
        return ResponseEntity.ok(ApiResponse.success("ALL", AppConstants.SUCCESS_DELETE));
    }
}
