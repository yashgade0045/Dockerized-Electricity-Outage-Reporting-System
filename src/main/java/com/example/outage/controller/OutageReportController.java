package com.example.outage.controller;

import com.example.outage.dto.OutageReportDto;
import com.example.outage.model.OutageReport;
import com.example.outage.model.OutageStatus;
import com.example.outage.service.OutageReportService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handles all CRUD operations for outage reports.
 * Role-based access is enforced at the method level using Spring Security's @PreAuthorize.
 * Server-side validation is performed on all form submissions.
 */
@Controller
@RequestMapping("/reports")
public class OutageReportController {

    private final OutageReportService outageReportService;

    public OutageReportController(OutageReportService outageReportService) {
        this.outageReportService = outageReportService;
    }

    private static final int DEFAULT_PAGE_SIZE = 10;

    // ── List / Search ────────────────────────────────────────────────────────

    /**
     * Display paginated list of reports, with optional keyword search and status filter.
     *
     * @param keyword  optional search term
     * @param status   optional status filter
     * @param page     0-indexed page number
     */
    @GetMapping
    public String listReports(@RequestParam(required = false) String keyword,
                               @RequestParam(required = false) OutageStatus status,
                               @RequestParam(defaultValue = "0") int page,
                               Model model) {

        Pageable pageable = PageRequest.of(page, DEFAULT_PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<OutageReport> reportPage = outageReportService.findReports(keyword, status, pageable);

        model.addAttribute("reports", reportPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", OutageStatus.values());
        model.addAttribute("pageTitle", "Outage Reports");
        return "reports/list";
    }

    // ── View Detail ──────────────────────────────────────────────────────────

    /**
     * Show full details of a single outage report.
     */
    @GetMapping("/{id}")
    public String viewReport(@PathVariable Long id, Model model) {
        OutageReport report = outageReportService.findByIdOrThrow(id);
        model.addAttribute("report", report);
        model.addAttribute("statuses", OutageStatus.values());
        model.addAttribute("pageTitle", "Report #" + id);
        return "reports/detail";
    }

    // ── Create ───────────────────────────────────────────────────────────────

    /**
     * Show the create-report form.
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("reportDto", new OutageReportDto());
        model.addAttribute("pageTitle", "Report an Outage");
        return "reports/create";
    }

    /**
     * Process the submitted create-report form.
     * On validation failure, re-displays the form with error messages.
     */
    @PostMapping("/new")
    public String createReport(@Valid @ModelAttribute("reportDto") OutageReportDto reportDto,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Report an Outage");
            return "reports/create";
        }

        OutageReport created = outageReportService.createReport(reportDto);
        redirectAttributes.addFlashAttribute("successMessage",
                "Outage report #" + created.getId() + " submitted successfully.");
        return "redirect:/reports/" + created.getId();
    }

    // ── Edit ─────────────────────────────────────────────────────────────────

    /**
     * Show the edit form pre-populated with existing report data.
     * Only ADMIN role may edit reports.
     */
    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditForm(@PathVariable Long id, Model model) {
        OutageReport report = outageReportService.findByIdOrThrow(id);

        OutageReportDto dto = OutageReportDto.builder()
                .id(report.getId())
                .title(report.getTitle())
                .location(report.getLocation())
                .description(report.getDescription())
                .reporterName(report.getReporterName())
                .status(report.getStatus())
                .build();

        model.addAttribute("reportDto", dto);
        model.addAttribute("report", report);
        model.addAttribute("pageTitle", "Edit Report #" + id);
        return "reports/edit";
    }

    /**
     * Process the edit form submission.
     */
    @PostMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateReport(@PathVariable Long id,
                                @Valid @ModelAttribute("reportDto") OutageReportDto reportDto,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Edit Report #" + id);
            return "reports/edit";
        }

        outageReportService.updateReport(id, reportDto);
        redirectAttributes.addFlashAttribute("successMessage",
                "Report #" + id + " updated successfully.");
        return "redirect:/reports/" + id;
    }

    // ── Status Update ────────────────────────────────────────────────────────

    /**
     * Update the status of an existing report.
     * Only ADMIN role may change report status.
     */
    @PostMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateStatus(@PathVariable Long id,
                                @RequestParam OutageStatus newStatus,
                                RedirectAttributes redirectAttributes) {

        outageReportService.updateStatus(id, newStatus);
        redirectAttributes.addFlashAttribute("successMessage",
                "Status updated to " + newStatus.name().replace('_', ' ') + ".");
        return "redirect:/reports/" + id;
    }

    // ── Delete ───────────────────────────────────────────────────────────────

    /**
     * Delete a report after confirmation.
     * Only ADMIN role may delete reports.
     */
    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteReport(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {

        outageReportService.deleteReport(id);
        redirectAttributes.addFlashAttribute("successMessage",
                "Report #" + id + " has been deleted.");
        return "redirect:/reports";
    }
}
