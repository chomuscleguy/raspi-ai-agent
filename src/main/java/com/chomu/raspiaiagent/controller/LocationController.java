package com.chomu.raspiaiagent.controller;

import com.chomu.raspiaiagent.entity.LocationSettings;
import com.chomu.raspiaiagent.repository.LocationSettingsRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/location")
public class LocationController {

    private final LocationSettingsRepository repository;

    public LocationController(LocationSettingsRepository repository) {
        this.repository = repository;
    }

    public record LocationRequest(Double latitude, Double longitude, String cityLabel, String source) {}

    @PostMapping
    public ResponseEntity<LocationSettings> setLocation(@RequestBody LocationRequest request) {
        LocationSettings saved = repository.save(new LocationSettings(
                request.latitude(),
                request.longitude(),
                request.cityLabel(),
                request.source() != null ? request.source() : "manual"
        ));
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<LocationSettings> getCurrentLocation() {
        return repository.findLatest()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}