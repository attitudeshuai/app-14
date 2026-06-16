package com.petfoster.controller;

import com.petfoster.common.ApiResponse;
import com.petfoster.common.PageResponse;
import com.petfoster.dto.DailyLogDTO;
import com.petfoster.entity.User;
import com.petfoster.service.DailyLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/fosterdailylogs")
@RequiredArgsConstructor
@Tag(name = "寄养日报管理", description = "寄养日报CRUD接口")
public class DailyLogController {

    private final DailyLogService logService;

    @GetMapping
    @Operation(summary = "获取寄养日报列表", description = "支持分页、搜索、筛选")
    public ApiResponse<PageResponse<DailyLogDTO.LogResponse>> getLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "logDate,desc") String sort,
            @RequestParam(required = false) Long requestId,
            @RequestParam(required = false) Long fostererId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.success(logService.getLogs(
                page, size, sort, requestId, fostererId, startDate, endDate));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取寄养日报详情")
    public ApiResponse<DailyLogDTO.LogResponse> getLogById(@PathVariable Long id) {
        return ApiResponse.success(logService.getLogById(id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "创建寄养日报(JSON格式)", description = "需要JWT认证，仅寄养人可创建，使用JSON格式提交，photos为外部图片URL，多个用逗号分隔")
    public ApiResponse<DailyLogDTO.LogResponse> createLogJson(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody DailyLogDTO.CreateLogRequest request) {
        return ApiResponse.success("创建成功", logService.createLog(user.getId(), request, null));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "创建寄养日报(表单格式)", description = "需要JWT认证，仅寄养人可创建，支持直接上传多张本地图片，图片将自动保存并写入日报")
    public ApiResponse<DailyLogDTO.LogResponse> createLogMultipart(
            @AuthenticationPrincipal User user,
            @Valid @ModelAttribute DailyLogDTO.CreateLogRequest request,
            @RequestPart(required = false) MultipartFile[] photoFiles) {
        return ApiResponse.success("创建成功", logService.createLog(user.getId(), request, photoFiles));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "更新寄养日报(JSON格式)", description = "需要JWT认证，仅创建者可操作，使用JSON格式提交")
    public ApiResponse<DailyLogDTO.LogResponse> updateLogJson(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestBody DailyLogDTO.UpdateLogRequest request) {
        return ApiResponse.success("更新成功", logService.updateLog(user.getId(), id, request, null, false));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "更新寄养日报(表单格式)", description = "需要JWT认证，仅创建者可操作，支持直接上传多张本地图片")
    public ApiResponse<DailyLogDTO.LogResponse> updateLogMultipart(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @ModelAttribute DailyLogDTO.UpdateLogRequest request,
            @RequestPart(required = false) MultipartFile[] photoFiles,
            @RequestParam(defaultValue = "false") boolean replacePhotos) {
        return ApiResponse.success("更新成功", logService.updateLog(user.getId(), id, request, photoFiles, replacePhotos));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除寄养日报", description = "需要JWT认证，仅创建者可操作")
    public ApiResponse<Void> deleteLog(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        logService.deleteLog(user.getId(), id);
        return ApiResponse.success("删除成功", null);
    }
}
