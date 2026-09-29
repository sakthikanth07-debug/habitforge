package com.personalhabitstreaktracker.habitforge.controller;

import com.personalhabitstreaktracker.habitforge.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/habits/dashboard")
    public Map<String, Object> getDashboardStats() {
        return dashboardService.getDashboardStats();
    }
}
