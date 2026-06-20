package com.petfoster.controller;

import com.petfoster.common.ApiResponse;
import com.petfoster.common.PageResponse;
import com.petfoster.dto.FosterRequestDTO;
import com.petfoster.entity.FosterRequest;
import com.petfoster.entity.User;
import com.petfoster.service.FosterRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/fosterrequests")
@RequiredArgsConstructor
@Tag(name = "寄养申请管理", description = "寄养申请CRUD及状态管理接口")
public class FosterRequestController {

    private final FosterRequestService requestService;

    @GetMapping
    @Operation(summary = "获取寄养申请列表", description = "支持分页、搜索、筛选（状态、寄养时间范围、宠物品种组合）和排序（创建时间、开始时间）")
    public ApiResponse<PageResponse<FosterRequestDTO.RequestResponse>> getRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @RequestParam(required = false) FosterRequest.Status status,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) Long fostererId,
            @RequestParam(required = false) Long petId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateTo,
            @RequestParam(required = false) String breed) {
        return ApiResponse.success(requestService.getRequests(
                page, size, sort, status, ownerId, fostererId, petId,
                startDateFrom, startDateTo, breed));
    }

    @GetMapping("/mine")
    @Operation(summary = "获取我的寄养申请", description = "获取与我相关的寄养申请（作为主人或寄养人），支持状态、寄养时间范围、宠物品种筛选")
    public ApiResponse<PageResponse<FosterRequestDTO.RequestResponse>> getMyRequests(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @RequestParam(required = false) FosterRequest.Status status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateTo,
            @RequestParam(required = false) String breed) {
        return ApiResponse.success(requestService.getMyRequests(
                user.getId(), page, size, sort,
                status, startDateFrom, startDateTo, breed));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取寄养申请详情")
    public ApiResponse<FosterRequestDTO.RequestResponse> getRequestById(@PathVariable Long id) {
        return ApiResponse.success(requestService.getRequestById(id));
    }

    @PostMapping("/check-conflict")
    @Operation(summary = "预约冲突检测", description = "检测指定宠物在指定时间段是否已有寄养申请冲突")
    public ApiResponse<FosterRequestDTO.CheckConflictResponse> checkConflict(
            @Valid @RequestBody FosterRequestDTO.CheckConflictRequest request) {
        return ApiResponse.success(requestService.checkConflict(request));
    }

    @PostMapping
    @Operation(summary = "创建寄养申请", description = "需要JWT认证，仅宠物主人可创建")
    public ApiResponse<FosterRequestDTO.RequestResponse> createRequest(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody FosterRequestDTO.CreateRequest request) {
        return ApiResponse.success("创建成功", requestService.createRequest(user.getId(), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新寄养申请", description = "需要JWT认证，仅申请人可操作")
    public ApiResponse<FosterRequestDTO.RequestResponse> updateRequest(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestBody FosterRequestDTO.UpdateRequest request) {
        return ApiResponse.success("更新成功", requestService.updateRequest(user.getId(), id, request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "修改寄养申请状态", description = "需要JWT认证，双方均可操作状态流转")
    public ApiResponse<FosterRequestDTO.RequestResponse> updateStatus(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody FosterRequestDTO.UpdateStatusRequest request) {
        return ApiResponse.success("状态更新成功",
                requestService.updateStatus(user.getId(), id, request.getStatus()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除寄养申请", description = "需要JWT认证，仅申请人可操作")
    public ApiResponse<Void> deleteRequest(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        requestService.deleteRequest(user.getId(), id);
        return ApiResponse.success("删除成功", null);
    }
}
