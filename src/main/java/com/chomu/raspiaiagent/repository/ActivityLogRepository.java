package com.chomu.raspiaiagent.repository;

import com.chomu.raspiaiagent.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findByCreatedAtAfterOrderByCreatedAtAsc(OffsetDateTime since);
}