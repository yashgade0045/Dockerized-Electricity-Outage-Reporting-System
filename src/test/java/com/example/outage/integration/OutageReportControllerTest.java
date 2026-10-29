package com.example.outage.integration;

import com.example.outage.model.OutageReport;
import com.example.outage.model.OutageStatus;
import com.example.outage.repository.OutageReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link com.example.outage.controller.OutageReportController}.
 * Uses MockMvc and an in-memory H2 database — no mocking of services.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("OutageReportController Integration Tests")
class OutageReportControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired OutageReportRepository repository;

    private OutageReport testReport;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        testReport = new OutageReport();
        testReport.setTitle("Test outage in Park Street");
        testReport.setLocation("Park Street, Test City");
        testReport.setDescription("Power cut affecting 50 homes in the area.");
        testReport.setReporterName("Integration Tester");
        testReport.setStatus(OutageStatus.REPORTED);
        testReport = repository.save(testReport);
    }

    // ── Authentication ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Unauthenticated users are redirected to login")
    void unauthenticatedRequest_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/reports"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    // ── List View ────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("GET /reports returns 200 and report list for authenticated user")
    void listReports_authenticated_returns200() throws Exception {
        mockMvc.perform(get("/reports"))
                .andExpect(status().isOk())
                .andExpect(view().name("reports/list"))
                .andExpect(model().attributeExists("reports"))
                .andExpect(content().string(containsString("Park Street")));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("GET /reports?keyword=Park returns matching reports")
    void searchByKeyword_returnsMatchingReports() throws Exception {
        mockMvc.perform(get("/reports").param("keyword", "Park"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("keyword", "Park"));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("GET /reports?status=REPORTED returns filtered reports")
    void filterByStatus_returnsFilteredReports() throws Exception {
        mockMvc.perform(get("/reports").param("status", "REPORTED"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("selectedStatus", OutageStatus.REPORTED));
    }

    // ── Create Report ────────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("GET /reports/new shows the create form")
    void showCreateForm_returns200() throws Exception {
        mockMvc.perform(get("/reports/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("reports/create"))
                .andExpect(model().attributeExists("reportDto"));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("POST /reports/new with valid data creates report and redirects")
    void createReport_validData_createsAndRedirects() throws Exception {
        long countBefore = repository.count();

        mockMvc.perform(post("/reports/new").with(csrf())
                        .param("title", "New outage on Test Avenue")
                        .param("location", "Test Avenue, Nashik")
                        .param("description", "Power failure affecting the entire street since morning.")
                        .param("reporterName", "New Reporter"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/reports/*"));

        assertThat(repository.count()).isEqualTo(countBefore + 1);
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("POST /reports/new with blank title shows validation error")
    void createReport_blankTitle_showsValidationError() throws Exception {
        mockMvc.perform(post("/reports/new").with(csrf())
                        .param("title", "")
                        .param("location", "Some Location")
                        .param("description", "Some description here.")
                        .param("reporterName", "Reporter"))
                .andExpect(status().isOk())
                .andExpect(view().name("reports/create"))
                .andExpect(model().attributeHasFieldErrors("reportDto", "title"));
    }

    // ── View Detail ──────────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("GET /reports/{id} shows report details")
    void viewReport_existingId_returns200() throws Exception {
        mockMvc.perform(get("/reports/{id}", testReport.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("reports/detail"))
                .andExpect(content().string(containsString(testReport.getTitle())));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("GET /reports/9999 returns 404 page")
    void viewReport_nonExistentId_returns404() throws Exception {
        mockMvc.perform(get("/reports/9999"))
                .andExpect(status().isNotFound());
    }

    // ── Role-Based Access ────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("USER role cannot access edit endpoint — returns 403")
    void editReport_asUser_returns403() throws Exception {
        mockMvc.perform(get("/reports/{id}/edit", testReport.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("ADMIN role can access edit endpoint — returns 200")
    void editReport_asAdmin_returns200() throws Exception {
        mockMvc.perform(get("/reports/{id}/edit", testReport.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("reports/edit"));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("USER role cannot update status — returns 403")
    void updateStatus_asUser_returns403() throws Exception {
        mockMvc.perform(post("/reports/{id}/status", testReport.getId()).with(csrf())
                        .param("newStatus", "RESOLVED"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("ADMIN role can update status — updates and redirects")
    void updateStatus_asAdmin_updatesAndRedirects() throws Exception {
        mockMvc.perform(post("/reports/{id}/status", testReport.getId()).with(csrf())
                        .param("newStatus", "IN_PROGRESS"))
                .andExpect(status().is3xxRedirection());

        OutageReport updated = repository.findById(testReport.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(OutageStatus.IN_PROGRESS);
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("USER role cannot delete report — returns 403")
    void deleteReport_asUser_returns403() throws Exception {
        mockMvc.perform(post("/reports/{id}/delete", testReport.getId()).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("ADMIN role can delete report — deletes and redirects")
    void deleteReport_asAdmin_deletesAndRedirects() throws Exception {
        long countBefore = repository.count();

        mockMvc.perform(post("/reports/{id}/delete", testReport.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/reports"));

        assertThat(repository.count()).isEqualTo(countBefore - 1);
    }

    // ── Dashboard ─────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("GET / returns dashboard with stats")
    void dashboard_authenticated_showsStats() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/index"))
                .andExpect(model().attributeExists("stats"));
    }
}
