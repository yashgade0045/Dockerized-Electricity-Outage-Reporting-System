package com.example.outage.unit;

import com.example.outage.dto.DashboardStatsDto;
import com.example.outage.dto.OutageReportDto;
import com.example.outage.exception.OutageReportNotFoundException;
import com.example.outage.model.OutageReport;
import com.example.outage.model.OutageStatus;
import com.example.outage.repository.OutageReportRepository;
import com.example.outage.service.OutageReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link OutageReportServiceImpl}.
 * Uses Mockito to isolate the service from the real database.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OutageReportService Unit Tests")
class OutageReportServiceTest {

    @Mock
    private OutageReportRepository repository;

    @InjectMocks
    private OutageReportServiceImpl service;

    private OutageReportDto validDto;
    private OutageReport savedReport;

    @BeforeEach
    void setUp() {
        validDto = new OutageReportDto();
        validDto.setTitle("Power outage in Test Street");
        validDto.setLocation("Test Street, Test City");
        validDto.setDescription("Complete power failure affecting 100 homes.");
        validDto.setReporterName("Test Reporter");
        validDto.setStatus(OutageStatus.REPORTED);

        savedReport = new OutageReport();
        savedReport.setId(1L);
        savedReport.setTitle(validDto.getTitle());
        savedReport.setLocation(validDto.getLocation());
        savedReport.setDescription(validDto.getDescription());
        savedReport.setReporterName(validDto.getReporterName());
        savedReport.setStatus(OutageStatus.REPORTED);
    }

    // ── Creation Tests ────────────────────────────────────────────────────────

    @Test
    @DisplayName("createReport: saves and returns report with REPORTED status")
    void createReport_validDto_savesAndReturns() {
        when(repository.save(any(OutageReport.class))).thenReturn(savedReport);

        OutageReport result = service.createReport(validDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(OutageStatus.REPORTED);
        assertThat(result.getTitle()).isEqualTo(validDto.getTitle());
        verify(repository, times(1)).save(any(OutageReport.class));
    }

    @Test
    @DisplayName("createReport: new report always starts with REPORTED status")
    void createReport_alwaysSetsReportedStatus() {
        validDto.setStatus(OutageStatus.RESOLVED); // Try to override status
        when(repository.save(any(OutageReport.class))).thenAnswer(inv -> {
            OutageReport r = inv.getArgument(0);
            assertThat(r.getStatus()).isEqualTo(OutageStatus.REPORTED);
            return savedReport;
        });

        service.createReport(validDto);
        verify(repository).save(any(OutageReport.class));
    }

    // ── Retrieval Tests ───────────────────────────────────────────────────────

    @Test
    @DisplayName("findByIdOrThrow: returns report when found")
    void findByIdOrThrow_existingId_returnsReport() {
        when(repository.findById(1L)).thenReturn(Optional.of(savedReport));

        OutageReport result = service.findByIdOrThrow(1L);

        assertThat(result).isEqualTo(savedReport);
    }

    @Test
    @DisplayName("findByIdOrThrow: throws OutageReportNotFoundException when not found")
    void findByIdOrThrow_missingId_throwsException() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByIdOrThrow(999L))
                .isInstanceOf(OutageReportNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("findReports: returns all reports when no filter")
    void findReports_noFilter_returnsAll() {
        Page<OutageReport> page = new PageImpl<>(List.of(savedReport));
        when(repository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<OutageReport> result = service.findReports(null, null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("findReports: delegates to keyword search when keyword provided")
    void findReports_withKeyword_callsKeywordSearch() {
        Page<OutageReport> page = new PageImpl<>(List.of(savedReport));
        when(repository.searchByKeyword(eq("MG Road"), any())).thenReturn(page);

        service.findReports("MG Road", null, PageRequest.of(0, 10));

        verify(repository).searchByKeyword(eq("MG Road"), any());
    }

    @Test
    @DisplayName("findReports: delegates to status filter when status provided")
    void findReports_withStatus_callsStatusFilter() {
        Page<OutageReport> page = new PageImpl<>(List.of(savedReport));
        when(repository.findByStatus(eq(OutageStatus.REPORTED), any())).thenReturn(page);

        service.findReports(null, OutageStatus.REPORTED, PageRequest.of(0, 10));

        verify(repository).findByStatus(eq(OutageStatus.REPORTED), any());
    }

    // ── Update Tests ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateReport: updates editable fields and saves")
    void updateReport_validDto_updatesFields() {
        when(repository.findById(1L)).thenReturn(Optional.of(savedReport));
        when(repository.save(any())).thenReturn(savedReport);

        validDto.setTitle("Updated Title");
        service.updateReport(1L, validDto);

        verify(repository).save(argThat(r -> r.getTitle().equals("Updated Title")));
    }

    @Test
    @DisplayName("updateStatus: changes status and saves")
    void updateStatus_validTransition_savesNewStatus() {
        when(repository.findById(1L)).thenReturn(Optional.of(savedReport));
        when(repository.save(any())).thenReturn(savedReport);

        service.updateStatus(1L, OutageStatus.IN_PROGRESS);

        verify(repository).save(argThat(r -> r.getStatus() == OutageStatus.IN_PROGRESS));
    }

    // ── Delete Tests ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteReport: removes existing report from repository")
    void deleteReport_existingId_deletesReport() {
        when(repository.findById(1L)).thenReturn(Optional.of(savedReport));

        service.deleteReport(1L);

        verify(repository).delete(savedReport);
    }

    @Test
    @DisplayName("deleteReport: throws if report does not exist")
    void deleteReport_missingId_throwsException() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteReport(999L))
                .isInstanceOf(OutageReportNotFoundException.class);
    }

    // ── Dashboard Tests ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getDashboardStats: returns correct counts from repository")
    void getDashboardStats_returnsLiveCounts() {
        when(repository.count()).thenReturn(7L);
        when(repository.countByStatus(OutageStatus.REPORTED)).thenReturn(3L);
        when(repository.countByStatus(OutageStatus.IN_PROGRESS)).thenReturn(2L);
        when(repository.countByStatus(OutageStatus.RESOLVED)).thenReturn(2L);

        DashboardStatsDto stats = service.getDashboardStats();

        assertThat(stats.getTotalReports()).isEqualTo(7L);
        assertThat(stats.getReportedCount()).isEqualTo(3L);
        assertThat(stats.getInProgressCount()).isEqualTo(2L);
        assertThat(stats.getResolvedCount()).isEqualTo(2L);
    }
}
