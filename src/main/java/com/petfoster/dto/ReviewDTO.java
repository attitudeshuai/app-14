package com.petfoster.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ReviewDTO {

    @Data
    public static class CreateReviewRequest {
        @NotNull(message = "寄养申请ID不能为空")
        private Long requestId;

        @NotNull(message = "被评价人ID不能为空")
        private Long revieweeId;

        @NotNull(message = "评分不能为空")
        @Min(value = 1, message = "评分最小为1")
        @Max(value = 5, message = "评分最大为5")
        private Integer rating;

        private String content;
    }

    @Data
    public static class UpdateReviewRequest {
        @Min(value = 1, message = "评分最小为1")
        @Max(value = 5, message = "评分最大为5")
        private Integer rating;

        private String content;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewResponse {
        private Long id;
        private Long requestId;
        private Long reviewerId;
        private String reviewerUsername;
        private Long revieweeId;
        private String revieweeUsername;
        private Integer rating;
        private String content;
        private String createdAt;
    }
}
