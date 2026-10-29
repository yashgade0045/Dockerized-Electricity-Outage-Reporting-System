package com.example.outage.dto;

import com.example.outage.model.OutageStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for creating and editing outage reports.
 */
public class OutageReportDto {

    private Long id;

    @NotBlank(message = "Title is required.")
    @Size(min = 5, max = 150, message = "Title must be between 5 and 150 characters.")
    private String title;

    @NotBlank(message = "Location is required.")
    @Size(min = 3, max = 255, message = "Location must be between 3 and 255 characters.")
    private String location;

    @NotBlank(message = "Description is required.")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters.")
    private String description;

    @NotBlank(message = "Reporter name is required.")
    @Size(min = 2, max = 100, message = "Reporter name must be between 2 and 100 characters.")
    private String reporterName;

    // Status is not required in the create form — the service always sets REPORTED on creation.
    // Status is only used by the edit form (admin only).
    private OutageStatus status;

    public OutageReportDto() {}

    public OutageReportDto(Long id, String title, String location, String description,
                           String reporterName, OutageStatus status) {
        this.id = id; this.title = title; this.location = location;
        this.description = description; this.reporterName = reporterName; this.status = status;
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

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String title, location, description, reporterName;
        private OutageStatus status;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder location(String location) { this.location = location; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder reporterName(String reporterName) { this.reporterName = reporterName; return this; }
        public Builder status(OutageStatus status) { this.status = status; return this; }

        public OutageReportDto build() {
            return new OutageReportDto(id, title, location, description, reporterName, status);
        }
    }
}
