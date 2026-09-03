package com.chomu.raspiaiagent.repository;

import com.chomu.raspiaiagent.entity.LocationSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface LocationSettingsRepository extends JpaRepository<LocationSettings, Long> {

    @Query("SELECT l FROM LocationSettings l ORDER BY l.updatedAt DESC LIMIT 1")
    Optional<LocationSettings> findLatest();
}