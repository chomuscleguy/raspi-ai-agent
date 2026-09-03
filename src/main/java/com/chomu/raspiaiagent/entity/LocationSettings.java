package com.chomu.raspiaiagent.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "location_settings")
public class LocationSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "city_label")
    private String cityLabel;

    @Column(nullable = false)
    private String source; // "auto" 또는 "manual"

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public LocationSettings() {}

    public LocationSettings(Double latitude, Double longitude, String cityLabel, String source) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.cityLabel = cityLabel;
        this.source = source;
        this.updatedAt = OffsetDateTime.now();
    }

    // Getters / Setters
    public Long getId() { return id; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getCityLabel() { return cityLabel; }
    public void setCityLabel(String cityLabel) { this.cityLabel = cityLabel; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}