package com.petfoster.service;

import com.petfoster.dto.ReputationDTO;
import com.petfoster.entity.User;
import com.petfoster.repository.FosterRequestRepository;
import com.petfoster.repository.FosterReviewRepository;
import com.petfoster.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReputationService {

    private final UserRepository userRepository;
    private final FosterRequestRepository requestRepository;
    private final FosterReviewRepository reviewRepository;

    private static final int MAX_REGISTER_SCORE = 20;
    private static final int MAX_COMPLETED_FOSTERS_SCORE = 25;
    private static final int MAX_RATING_SCORE = 30;
    private static final int DEFAULT_DEDUCTION_PER_BREACH = 15;
    private static final int BASELINE_SCORE = 25;

    public ReputationDTO calculateReputation(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        long registerDays = calculateRegisterDays(user);
        int registerScore = calculateRegisterScore(registerDays);

        long completedFosters = requestRepository.countCompletedByUserId(userId);
        int completedFostersScore = calculateCompletedFostersScore(completedFosters);

        Double avgRating = reviewRepository.findAverageRatingByRevieweeId(userId);
        long reviewCount = reviewRepository.countByRevieweeId(userId);
        int ratingScore = calculateRatingScore(avgRating, reviewCount);

        long cancelledCount = requestRepository.countCancelledByUserId(userId);
        int defaultDeduction = calculateDefaultDeduction(cancelledCount);

        int totalScore = calculateTotalScore(registerScore, completedFostersScore, ratingScore, defaultDeduction);

        ReputationDTO.Level level = ReputationDTO.Level.fromScore(totalScore);

        return ReputationDTO.builder()
                .totalScore(totalScore)
                .level(level.getLabel())
                .registerMonthsScore(registerScore)
                .completedFostersScore(completedFostersScore)
                .averageRatingScore(ratingScore)
                .defaultDeduction(defaultDeduction)
                .registerDays(registerDays)
                .completedFosters(completedFosters)
                .averageRating(avgRating != null ? Math.round(avgRating * 100.0) / 100.0 : 0.0)
                .reviewCount(reviewCount)
                .defaultCount((int) cancelledCount)
                .build();
    }

    private long calculateRegisterDays(User user) {
        LocalDateTime createdAt = user.getCreatedAt();
        if (createdAt == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
    }

    private int calculateRegisterScore(long registerDays) {
        if (registerDays < 30) {
            return 5;
        } else if (registerDays < 90) {
            return 10;
        } else if (registerDays < 180) {
            return 15;
        } else {
            return MAX_REGISTER_SCORE;
        }
    }

    private int calculateCompletedFostersScore(long completedFosters) {
        if (completedFosters == 0) {
            return 0;
        } else if (completedFosters <= 3) {
            return 10;
        } else if (completedFosters <= 10) {
            return 18;
        } else {
            return MAX_COMPLETED_FOSTERS_SCORE;
        }
    }

    private int calculateRatingScore(Double avgRating, long reviewCount) {
        if (reviewCount == 0 || avgRating == null) {
            return 10;
        }
        if (avgRating < 2.0) {
            return 10;
        } else if (avgRating < 3.0) {
            return 15;
        } else if (avgRating < 4.0) {
            return 22;
        } else {
            return MAX_RATING_SCORE;
        }
    }

    private int calculateDefaultDeduction(long cancelledCount) {
        return (int) (cancelledCount * DEFAULT_DEDUCTION_PER_BREACH);
    }

    private int calculateTotalScore(int registerScore, int completedScore,
                                    int ratingScore, int defaultDeduction) {
        int total = BASELINE_SCORE + registerScore + completedScore + ratingScore - defaultDeduction;
        return Math.max(0, Math.min(100, total));
    }
}
