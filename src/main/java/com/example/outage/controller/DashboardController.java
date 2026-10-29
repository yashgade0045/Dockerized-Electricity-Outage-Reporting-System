package com.example.outage.controller;

import com.example.outage.dto.DashboardStatsDto;
import com.example.outage.service.OutageReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Handles requests to the main dashboard page.
 * Statistics are loaded from the database on every request — no hardcoded values.
 */
@Controller
@RequestMapping("/")
public class DashboardController {

    private final OutageReportService outageReportService;

    public DashboardController(OutageReportService outageReportService) {
        this.outageReportService = outageReportService;
    }

    /**
     * Display the dashboard with live status summary cards.
     */
    @GetMapping
    public String dashboard(Model model) {
        DashboardStatsDto stats = outageReportService.getDashboardStats();
        model.addAttribute("stats", stats);
        model.addAttribute("pageTitle", "Dashboard");
        return "dashboard/index";
    }
}
