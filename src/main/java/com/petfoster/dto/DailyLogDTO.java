package com.petfoster.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class DailyLogDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateLogRequest {
        @NotNull(message = "寄养申请ID不能为空")
        private Long requestId;

        @NotNull(message = "日志日期不能为空")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate logDate;

        private String food;
        private String mood;
        private String photos;
        private String note;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateLogRequest {
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate logDate;
        private String food;
        private String mood;
        private String photos;
        private String note;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogResponse {
        private Long id;
        private Long requestId;
        private Long fostererId;
        private String fostererUsername;
        private LocalDate logDate;
        private String food;
        private String mood;
        private String photos;
        private String note;
    }
}
