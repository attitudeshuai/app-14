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

    @PostMapping
    @Operation(summary = "创建寄养日报(JSON格式)", description = "需要JWT认证，仅寄养人可创建，使用JSON格式提交，photos为外部图片URL，多个用逗号分隔")
    public ApiResponse<DailyLogDTO.LogResponse> createLog(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody DailyLogDTO.CreateLogRequest request) {
        return ApiResponse.success("创建成功", logService.createLog(user.getId(), request));
    }

    @PostMapping(value = "/with-photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "创建寄养日报(带图片上传)", description = "需要JWT认证，仅寄养人可创建，支持直接上传多张本地图片，图片将自动保存并写入日报")
    public ApiResponse<DailyLogDTO.LogResponse> createLogWithPhotos(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Long requestId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate logDate,
            @RequestParam(required = false) String food,
            @RequestParam(required = false) String mood,
            @RequestParam(required = false) String photos,
            @RequestParam(required = false) String note,
            @RequestPart(required = false) MultipartFile[] photoFiles) {
        DailyLogDTO.CreateLogRequest request = DailyLogDTO.CreateLogRequest.builder()
                .requestId(requestId)
                .logDate(logDate)
                .food(food)
                .mood(mood)
                .photos(photos)
                .note(note)
                .build();
        return ApiResponse.success("创建成功", logService.createLogWithPhotos(user.getId(), request, photoFiles));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新寄养日报(JSON格式)", description = "需要JWT认证，仅创建者可操作，使用JSON格式提交")
    public ApiResponse<DailyLogDTO.LogResponse> updateLog(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestBody DailyLogDTO.UpdateLogRequest request) {
        return ApiResponse.success("更新成功", logService.updateLog(user.getId(), id, request));
    }

    @PutMapping(value = "/{id}/with-photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "更新寄养日报(带图片上传)", description = "需要JWT认证，仅创建者可操作，支持直接上传多张本地图片")
    public ApiResponse<DailyLogDTO.LogResponse> updateLogWithPhotos(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate logDate,
            @RequestParam(required = false) String food,
            @RequestParam(required = false) String mood,
            @RequestParam(required = false) String photos,
            @RequestParam(required = false) String note,
            @RequestPart(required = false) MultipartFile[] photoFiles,
            @RequestParam(defaultValue = "false") boolean replacePhotos) {
        DailyLogDTO.UpdateLogRequest request = DailyLogDTO.UpdateLogRequest.builder()
                .logDate(logDate)
                .food(food)
                .mood(mood)
                .photos(photos)
                .note(note)
                .build();
        return ApiResponse.success("更新成功", logService.updateLogWithPhotos(user.getId(), id, request, photoFiles, replacePhotos));
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
