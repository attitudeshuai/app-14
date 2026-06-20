package com.petfoster.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private Type type;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(name = "related_id")
    private Long relatedId;

    @Column(name = "related_type", length = 50)
    @Enumerated(EnumType.STRING)
    private RelatedType relatedType;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum Type {
        FOSTER_REQUEST_STATUS,
        DAILY_LOG_CREATED,
        DAILY_LOG_UPDATED,
        FOSTER_REQUEST_CREATED,
        FOSTER_REQUEST_TIMEOUT,
        DAILY_LOG_REMINDER,
        DAILY_LOG_MISSED_REMINDER
    }

    public enum RelatedType {
        FOSTER_REQUEST,
        DAILY_LOG
    }
}
