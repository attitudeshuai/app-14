package com.petfoster.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "foster_daily_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FosterDailyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Column(name = "fosterer_id", nullable = false)
    private Long fostererId;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(length = 500)
    private String food;

    @Column(length = 200)
    private String mood;

    @Column(name = "photos", length = 2000)
    private String photos;

    @Column(length = 2000)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", insertable = false, updatable = false)
    private FosterRequest fosterRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fosterer_id", insertable = false, updatable = false)
    private User fosterer;
}
