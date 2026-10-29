package com.example.outage.service;

import com.example.outage.dto.DashboardStatsDto;
import com.example.outage.dto.OutageReportDto;
import com.example.outage.exception.OutageReportNotFoundException;
import com.example.outage.model.OutageReport;
import com.example.outage.model.OutageStatus;
import com.example.outage.repository.OutageReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * Default implementation of {@link OutageReportService}.
 */
@Service
@Transactional(readOnly = true)
public class OutageReportServiceImpl implements OutageReportService {

    private static final Logger log = LoggerFactory.getLogger(OutageReportServiceImpl.class);

    private final OutageReportRepository repository;

    public OutageReportServiceImpl(OutageReportRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<OutageReport> findReports(String keyword, OutageStatus status, Pageable pageable) {
        boolean hasKeyword = StringUtils.hasText(keyword);
        boolean hasStatus  = status != null;

        if (hasKeyword && hasStatus) {
            return repository.searchByKeywordAndStatus(keyword.trim(), status, pageable);
        } else if (hasKeyword) {
            return repository.searchByKeyword(keyword.trim(), pageable);
        } else if (hasStatus) {
            return repository.findByStatus(status, pageable);
        } else {
            return repository.findAll(pageable);
        }
    }

    @Override
    public Optional<OutageReport> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public OutageReport findByIdOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new OutageReportNotFoundException(id));
    }

    @Override
    @Transactional
    public OutageReport createReport(OutageReportDto dto) {
        OutageReport report = OutageReport.builder()
                .title(dto.getTitle().trim())
                .location(dto.getLocation().trim())
                .description(dto.getDescription().trim())
                .reporterName(dto.getReporterName().trim())
                .status(OutageStatus.REPORTED)
                .build();

        OutageReport saved = repository.save(report);
        log.info("Created outage report ID={} by '{}'", saved.getId(), saved.getReporterName());
        return saved;
    }

    @Override
    @Transactional
    public OutageReport updateReport(Long id, OutageReportDto dto) {
        OutageReport existing = findByIdOrThrow(id);
        existing.setTitle(dto.getTitle().trim());
        existing.setLocation(dto.getLocation().trim());
        existing.setDescription(dto.getDescription().trim());
        existing.setReporterName(dto.getReporterName().trim());
        OutageReport updated = repository.save(existing);
        log.info("Updated outage report ID={}", updated.getId());
        return updated;
    }

    @Override
    @Transactional
    public OutageReport updateStatus(Long id, OutageStatus newStatus) {
        OutageReport existing = findByIdOrThrow(id);
        OutageStatus oldStatus = existing.getStatus();
        existing.setStatus(newStatus);
        OutageReport updated = repository.save(existing);
        log.info("Status changed for report ID={}: {} -> {}", id, oldStatus, newStatus);
        return updated;
    }

    @Override
    @Transactional
    public void deleteReport(Long id) {
        OutageReport existing = findByIdOrThrow(id);
        repository.delete(existing);
        log.info("Deleted outage report ID={}", id);
    }

    @Override
    public DashboardStatsDto getDashboardStats() {
        long total      = repository.count();
        long reported   = repository.countByStatus(OutageStatus.REPORTED);
        long inProgress = repository.countByStatus(OutageStatus.IN_PROGRESS);
        long resolved   = repository.countByStatus(OutageStatus.RESOLVED);

        return DashboardStatsDto.builder()
                .totalReports(total)
                .reportedCount(reported)
                .inProgressCount(inProgress)
                .resolvedCount(resolved)
                .build();
    }
}
