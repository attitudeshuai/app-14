package com.petfoster.dto;

import com.petfoster.entity.FosterRequest;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

public class FosterRequestDTO {

    @Data
    public static class CheckConflictRequest {
        @NotNull(message = "宠物ID不能为空")
        private Long petId;

        @NotNull(message = "开始日期不能为空")
        private LocalDate startDate;

        @NotNull(message = "结束日期不能为空")
        private LocalDate endDate;

        private Long excludeRequestId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConflictInfo {
        private Long requestId;
        private LocalDate startDate;
        private LocalDate endDate;
        private FosterRequest.Status status;
        private Long fostererId;
        private String fostererUsername;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CheckConflictResponse {
        private boolean hasConflict;
        private List<ConflictInfo> conflicts;
    }

    @Data
    public static class CreateRequest {
        @NotNull(message = "宠物ID不能为空")
        private Long petId;

        private Long fostererId;

        @NotNull(message = "开始日期不能为空")
        private LocalDate startDate;

        @NotNull(message = "结束日期不能为空")
        private LocalDate endDate;

        private String dailyCareNotes;
    }

    @Data
    public static class UpdateRequest {
        private Long fostererId;
        private LocalDate startDate;
        private LocalDate endDate;
        private String dailyCareNotes;
    }

    @Data
    public static class UpdateStatusRequest {
        @NotNull(message = "状态不能为空")
        private FosterRequest.Status status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestResponse {
        private Long id;
        private Long petId;
        private String petName;
        private Long ownerId;
        private String ownerUsername;
        private Long fostererId;
        private String fostererUsername;
        private LocalDate startDate;
        private LocalDate endDate;
        private String dailyCareNotes;
        private FosterRequest.Status status;
        private String createdAt;
    }
}
