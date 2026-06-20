package com.petfoster.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "foster_reviews")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FosterReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Column(name = "reviewer_id", nullable = false)
    private Long reviewerId;

    @Column(name = "reviewee_id", nullable = false)
    private Long revieweeId;

    @Column(name = "responsibility_rating", nullable = false)
    private Integer responsibilityRating;

    @Column(name = "communication_rating", nullable = false)
    private Integer communicationRating;

    @Column(name = "pet_condition_rating", nullable = false)
    private Integer petConditionRating;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 2000)
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", insertable = false, updatable = false)
    private FosterRequest fosterRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", insertable = false, updatable = false)
    private User reviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewee_id", insertable = false, updatable = false)
    private User reviewee;
}
