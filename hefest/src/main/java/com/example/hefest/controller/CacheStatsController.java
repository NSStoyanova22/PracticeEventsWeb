package com.example.hefest.controller;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.springframework.cache.CacheManager;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CacheStatsController {
    private final CacheManager cacheManager;
    public CacheStatsController(CacheManager cacheManager) { this.cacheManager = cacheManager; }

    @GetMapping("/admin/cache-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> stats() {
        var cache = (org.springframework.cache.caffeine.CaffeineCache) cacheManager.getCache("events");
        CacheStats stats = cache.getNativeCache().stats();
        return Map.of("requestCount", stats.requestCount(), "hitCount", stats.hitCount(),
                "missCount", stats.missCount(), "hitRate", stats.hitRate(), "evictionCount", stats.evictionCount());
    }
}
