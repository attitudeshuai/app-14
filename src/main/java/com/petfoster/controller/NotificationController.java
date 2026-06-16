package com.petfoster.controller;

import com.petfoster.common.ApiResponse;
import com.petfoster.common.PageResponse;
import com.petfoster.dto.NotificationDTO;
import com.petfoster.entity.User;
import com.petfoster.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "站内消息管理", description = "站内消息通知接口")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "获取我的消息列表", description = "需要JWT认证，分页查询当前用户的所有消息")
    public ApiResponse<PageResponse<NotificationDTO.NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @RequestParam(required = false) Boolean isRead) {
        return ApiResponse.success(notificationService.getMyNotifications(
                user.getId(), page, size, sort, isRead));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "获取未读消息数量", description = "需要JWT认证，获取当前用户的未读消息总数")
    public ApiResponse<NotificationDTO.UnreadCountResponse> getUnreadCount(
            @AuthenticationPrincipal User user) {
        return ApiResponse.success(notificationService.getUnreadCount(user.getId()));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "标记单条消息为已读", description = "需要JWT认证，标记指定消息为已读")
    public ApiResponse<Void> markAsRead(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        notificationService.markAsRead(user.getId(), id);
        return ApiResponse.success("标记成功", null);
    }

    @PatchMapping("/read-all")
    @Operation(summary = "标记所有消息为已读", description = "需要JWT认证，将当前用户的所有消息标记为已读")
    public ApiResponse<Void> markAllAsRead(@AuthenticationPrincipal User user) {
        notificationService.markAllAsRead(user.getId());
        return ApiResponse.success("全部标记成功", null);
    }
}
