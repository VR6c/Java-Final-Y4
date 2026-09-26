package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheStatResponse {
    private String cacheName;
    private long estimatedSize;
    private long requestCount;
    private long hitCount;
    private long missCount;
    private double hitRatePercentage;
    private long evictionCount;
}
