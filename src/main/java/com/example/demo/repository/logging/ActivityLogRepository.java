package com.example.demo.repository.logging;

import com.example.demo.entity.logging.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    @Query("SELECT a FROM ActivityLog a WHERE " +
            "(:username IS NULL OR LOWER(a.username) LIKE LOWER(CONCAT('%', :username, '%'))) AND " +
            "(:action IS NULL OR UPPER(a.action) = UPPER(:action)) AND " +
            "(:resource IS NULL OR UPPER(a.resource) = UPPER(:resource))")
    Page<ActivityLog> searchActivities(
            @Param("username") String username,
            @Param("action") String action,
            @Param("resource") String resource,
            Pageable pageable
    );
}
