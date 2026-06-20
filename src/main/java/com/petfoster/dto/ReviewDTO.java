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

        @NotNull(message = "责任心评分不能为空")
        @Min(value = 1, message = "责任心评分最小为1")
        @Max(value = 5, message = "责任心评分最大为5")
        private Integer responsibilityRating;

        @NotNull(message = "沟通能力评分不能为空")
        @Min(value = 1, message = "沟通能力评分最小为1")
        @Max(value = 5, message = "沟通能力评分最大为5")
        private Integer communicationRating;

        @NotNull(message = "宠物状态反馈评分不能为空")
        @Min(value = 1, message = "宠物状态反馈评分最小为1")
        @Max(value = 5, message = "宠物状态反馈评分最大为5")
        private Integer petConditionRating;

        private String content;
    }

    @Data
    public static class UpdateReviewRequest {
        @Min(value = 1, message = "责任心评分最小为1")
        @Max(value = 5, message = "责任心评分最大为5")
        private Integer responsibilityRating;

        @Min(value = 1, message = "沟通能力评分最小为1")
        @Max(value = 5, message = "沟通能力评分最大为5")
        private Integer communicationRating;

        @Min(value = 1, message = "宠物状态反馈评分最小为1")
        @Max(value = 5, message = "宠物状态反馈评分最大为5")
        private Integer petConditionRating;

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
        private Integer responsibilityRating;
        private Integer communicationRating;
        private Integer petConditionRating;
        private Integer rating;
        private String content;
        private String createdAt;
    }
}
