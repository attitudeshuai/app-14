package com.petfoster.dto;

import com.petfoster.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class NotificationDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationResponse {
        private Long id;
        private Long userId;
        private Notification.Type type;
        private String title;
        private String content;
        private Long relatedId;
        private Notification.RelatedType relatedType;
        private Boolean isRead;
        private String createdAt;
    }

    @Data
    public static class MarkReadRequest {
        private Long id;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnreadCountResponse {
        private long count;
    }
}
