package com.petfoster.event;

import com.petfoster.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {

    private List<NotificationEntry> entries;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationEntry {
        private Long userId;
        private Notification.Type type;
        private String title;
        private String content;
        private Long relatedId;
        private Notification.RelatedType relatedType;
    }

    public static NotificationEntry entry(Long userId, Notification.Type type, String title,
                                            String content, Long relatedId, Notification.RelatedType relatedType) {
        return new NotificationEntry(userId, type, title, content, relatedId, relatedType);
    }
}
