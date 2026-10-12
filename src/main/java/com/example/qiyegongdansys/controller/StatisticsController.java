package com.example.qiyegongdansys.controller;

import com.example.qiyegongdansys.common.ApiResponse;
import com.example.qiyegongdansys.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<Map<String, Object>> overview() {
        return ApiResponse.success(statisticsService.overview());
    }

    @GetMapping("/by-type")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<Map<String, Object>> byType() {
        return ApiResponse.success(statisticsService.byType());
    }

    @GetMapping("/by-agent")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<Map<String, Object>> byAgent() {
        return ApiResponse.success(statisticsService.byAgent());
    }

    @GetMapping("/by-day")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<Map<String, Object>> byDay(@RequestParam(defaultValue = "7") int days) {
        return ApiResponse.success(statisticsService.byDay(days));
    }

    @GetMapping("/assign-mode")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<Map<String, Object>> assignMode() {
        return ApiResponse.success(statisticsService.assignModeDistribution());
    }
}