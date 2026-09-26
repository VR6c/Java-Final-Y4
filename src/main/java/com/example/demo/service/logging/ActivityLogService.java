package com.example.demo.service.logging;

import com.example.demo.dto.response.logging.ActivityLogResponse;
import com.example.demo.entity.logging.ActivityLog;
import com.example.demo.repository.logging.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private static final Logger log = LoggerFactory.getLogger(ActivityLogService.class);

    private final ActivityLogRepository activityLogRepository;
    private final ModelMapper modelMapper;

    /**
     * Persists an activity log asynchronously in a background thread
     * so it never adds latency to client HTTP responses.
     */
    @Async
    public void recordActivityAsync(
            String username,
            String action,
            String resource,
            String endpoint,
            String httpMethod,
            int statusCode,
            Long durationMs,
            String ipAddress,
            String errorMessage
    ) {
        try {
            ActivityLog activityLog = ActivityLog.builder()
                    .username(username)
                    .action(action)
                    .resource(resource)
                    .endpoint(endpoint)
                    .httpMethod(httpMethod)
                    .statusCode(statusCode)
                    .durationMs(durationMs)
                    .ipAddress(ipAddress)
                    .errorMessage(errorMessage)
                    .createdAt(LocalDateTime.now())
                    .build();

            activityLogRepository.save(activityLog);
        } catch (Exception ex) {
            log.warn("Failed to persist activity log to database: {}", ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Page<ActivityLogResponse> getAll(Pageable pageable) {
        return activityLogRepository.findAll(pageable)
                .map(entity -> modelMapper.map(entity, ActivityLogResponse.class));
    }

    @Transactional(readOnly = true)
    public Page<ActivityLogResponse> search(String username, String action, String resource, Pageable pageable) {
        return activityLogRepository.searchActivities(username, action, resource, pageable)
                .map(entity -> modelMapper.map(entity, ActivityLogResponse.class));
    }
}
