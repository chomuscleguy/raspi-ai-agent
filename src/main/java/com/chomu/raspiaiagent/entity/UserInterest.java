package com.chomu.raspiaiagent.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_interests")
public class UserInterest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String keyword;

    @Column(name = "mentioned_at", nullable = false)
    private OffsetDateTime mentionedAt;

    @Column(name = "last_checked_at")
    private OffsetDateTime lastCheckedAt;

    @Column(nullable = false)
    private Boolean active = true;

    public UserInterest() {}

    public UserInterest(String keyword) {
        this.keyword = keyword;
        this.mentionedAt = OffsetDateTime.now();
        this.active = true;
    }

    public Long getId() { return id; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public OffsetDateTime getMentionedAt() { return mentionedAt; }
    public void setMentionedAt(OffsetDateTime mentionedAt) { this.mentionedAt = mentionedAt; }
    public OffsetDateTime getLastCheckedAt() { return lastCheckedAt; }
    public void setLastCheckedAt(OffsetDateTime lastCheckedAt) { this.lastCheckedAt = lastCheckedAt; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}