package com.example.demo.entity.logging;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity to persist audit activities into the PostgreSQL database.
 */
@Entity
@Table(name = "activity_logs", indexes = {
        @Index(name = "idx_activity_username", columnList = "username"),
        @Index(name = "idx_activity_action", columnList = "action"),
        @Index(name = "idx_activity_resource", columnList = "resource"),
        @Index(name = "idx_activity_created_at", columnList = "created_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_id")
    private Long id;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "action", length = 50, nullable = false)
    private String action;

    @Column(name = "resource", length = 50, nullable = false)
    private String resource;

    @Column(name = "endpoint", length = 255, nullable = false)
    private String endpoint;

    @Column(name = "http_method", length = 10, nullable = false)
    private String httpMethod;

    @Column(name = "status_code", nullable = false)
    private int statusCode;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
