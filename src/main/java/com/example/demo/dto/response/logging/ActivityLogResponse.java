package com.example.demo.dto.response.logging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogResponse {
    private Long id;
    private String username;
    private String action;
    private String resource;
    private String endpoint;
    private String httpMethod;
    private int statusCode;
    private Long durationMs;
    private String ipAddress;
    private String errorMessage;
    private LocalDateTime createdAt;
}
