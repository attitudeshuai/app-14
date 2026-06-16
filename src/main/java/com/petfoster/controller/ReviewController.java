package com.petfoster.controller;

import com.petfoster.common.ApiResponse;
import com.petfoster.common.PageResponse;
import com.petfoster.dto.ReviewDTO;
import com.petfoster.entity.User;
import com.petfoster.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fosterreviews")
@RequiredArgsConstructor
@Tag(name = "寄养评价管理", description = "寄养评价CRUD接口")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    @Operation(summary = "获取评价列表", description = "支持分页、搜索、筛选")
    public ApiResponse<PageResponse<ReviewDTO.ReviewResponse>> getReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @RequestParam(required = false) Long requestId,
            @RequestParam(required = false) Long reviewerId,
            @RequestParam(required = false) Long revieweeId,
            @RequestParam(required = false) Integer minRating,
            @RequestParam(required = false) Integer maxRating) {
        return ApiResponse.success(reviewService.getReviews(
                page, size, sort, requestId, reviewerId, revieweeId, minRating, maxRating));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取评价详情")
    public ApiResponse<ReviewDTO.ReviewResponse> getReviewById(@PathVariable Long id) {
        return ApiResponse.success(reviewService.getReviewById(id));
    }

    @PostMapping
    @Operation(summary = "创建寄养评价", description = "需要JWT认证，寄养完成后双方互评")
    public ApiResponse<ReviewDTO.ReviewResponse> createReview(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ReviewDTO.CreateReviewRequest request) {
        return ApiResponse.success("评价成功", reviewService.createReview(user.getId(), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新评价", description = "需要JWT认证，仅评价者可操作")
    public ApiResponse<ReviewDTO.ReviewResponse> updateReview(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestBody ReviewDTO.UpdateReviewRequest request) {
        return ApiResponse.success("更新成功", reviewService.updateReview(user.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除评价", description = "需要JWT认证，仅评价者可操作")
    public ApiResponse<Void> deleteReview(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        reviewService.deleteReview(user.getId(), id);
        return ApiResponse.success("删除成功", null);
    }
}
