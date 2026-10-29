package com.example.outage.service;

import com.example.outage.dto.DashboardStatsDto;
import com.example.outage.dto.OutageReportDto;
import com.example.outage.model.OutageReport;
import com.example.outage.model.OutageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Service interface defining all business operations for outage reports.
 */
public interface OutageReportService {

    /** Return paginated, optionally filtered/searched list of reports. */
    Page<OutageReport> findReports(String keyword, OutageStatus status, Pageable pageable);

    /** Retrieve a single report by ID, or empty if not found. */
    Optional<OutageReport> findById(Long id);

    /** Retrieve a single report or throw {@link com.example.outage.exception.OutageReportNotFoundException}. */
    OutageReport findByIdOrThrow(Long id);

    /** Create and persist a new outage report from validated DTO data. */
    OutageReport createReport(OutageReportDto dto);

    /** Update an existing report's editable fields from validated DTO data. */
    OutageReport updateReport(Long id, OutageReportDto dto);

    /** Change the status of a report (admin-only operation). */
    OutageReport updateStatus(Long id, OutageStatus newStatus);

    /** Delete a report by ID (admin-only operation). */
    void deleteReport(Long id);

    /** Compute dashboard counts from live database data. */
    DashboardStatsDto getDashboardStats();
}
