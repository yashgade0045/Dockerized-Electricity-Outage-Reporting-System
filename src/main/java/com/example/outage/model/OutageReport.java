package com.example.outage.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * JPA entity representing a single electricity outage report submitted by a citizen.
 * Persisted to the H2 database (local dev) or a configured RDBMS in production.
 */
@Entity
@Table(name = "outage_report")
public class OutageReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 255)
    private String location;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(name = "reporter_name", nullable = false, length = 100)
    private String reporterName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutageStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public OutageReport() {}

    public OutageReport(Long id, String title, String location, String description,
                        String reporterName, OutageStatus status,
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.location = location;
        this.description = description;
        this.reporterName = reporterName;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }
    public OutageStatus getStatus() { return status; }
    public void setStatus(OutageStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String title, location, description, reporterName;
        private OutageStatus status;
        private LocalDateTime createdAt, updatedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder location(String location) { this.location = location; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder reporterName(String reporterName) { this.reporterName = reporterName; return this; }
        public Builder status(OutageStatus status) { this.status = status; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public OutageReport build() {
            OutageReport r = new OutageReport();
            r.id = this.id; r.title = this.title; r.location = this.location;
            r.description = this.description; r.reporterName = this.reporterName;
            r.status = this.status; r.createdAt = this.createdAt; r.updatedAt = this.updatedAt;
            return r;
        }
    }
}
