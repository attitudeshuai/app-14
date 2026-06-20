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

    @GetMapping("/popular-breeds")
    @Operation(summary = "热门宠物品种排行", description = "按寄养申请数量统计热门宠物品种排行榜")
    public ApiResponse<StatsDTO.PopularBreedStats> getPopularBreeds(
            @RequestParam(defaultValue = "10") int topN) {
        return ApiResponse.success(statsService.getPopularBreedStats(topN));
    }

    @GetMapping("/foster-duration")
    @Operation(summary = "平均寄养时长统计", description = "统计寄养时长分布、平均值、中位数及按品种/物种分类的平均时长")
    public ApiResponse<StatsDTO.FosterDurationStats> getFosterDuration() {
        return ApiResponse.success(statsService.getFosterDurationStats());
    }
}
