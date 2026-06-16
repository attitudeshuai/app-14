package com.petfoster.controller;

import com.petfoster.common.ApiResponse;
import com.petfoster.dto.StatsDTO;
import com.petfoster.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@Tag(name = "统计数据", description = "数据看板与趋势统计接口")
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/overview")
    @Operation(summary = "总览统计", description = "获取平台基础数据统计（用户数、宠物数、寄养数、评价统计等")
    public ApiResponse<StatsDTO.OverviewStats> getOverview() {
        return ApiResponse.success(statsService.getOverviewStats());
    }

    @GetMapping("/trend")
    @Operation(summary = "趋势统计", description = "按时间范围获取每日数据趋势")
    public ApiResponse<StatsDTO.TrendStats> getTrend(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.success(statsService.getTrendStats(startDate, endDate));
    }
}
