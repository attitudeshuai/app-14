package com.petfoster.service;

import com.petfoster.dto.StatsDTO;
import com.petfoster.entity.FosterRequest;
import com.petfoster.entity.Pet;
import com.petfoster.entity.User;
import com.petfoster.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {

    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final FosterRequestRepository requestRepository;
    private final FosterReviewRepository reviewRepository;
    private final FosterDailyLogRepository dailyLogRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public StatsDTO.OverviewStats getOverviewStats() {
        long totalUsers = userRepository.count();
        long totalPets = petRepository.count();
        long totalRequests = requestRepository.count();
        long totalCompletedRequests = requestRepository.countByStatus(FosterRequest.Status.Completed);
        long totalReviews = reviewRepository.count();

        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.plusDays(1).atStartOfDay();

        long todayNewRequests = 0;
        for (FosterRequest req : requestRepository.findAll()) {
            LocalDateTime createdAt = req.getCreatedAt();
            if (createdAt != null && !createdAt.isBefore(todayStart) && createdAt.isBefore(todayEnd)) {
                todayNewRequests++;
            }
        }

        Map<String, Long> requestStatusCount = Arrays.stream(FosterRequest.Status.values())
                .collect(Collectors.toMap(
                        Enum::name,
                        status -> requestRepository.countByStatus(status)
                ));

        Map<String, Long> petSpeciesCount = petRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        Pet::getSpecies,
                        Collectors.counting()
                ));

        List<StatsDTO.TopUser> topRatedUsers = calculateTopRatedUsers(10);

        return StatsDTO.OverviewStats.builder()
                .totalUsers(totalUsers)
                .totalPets(totalPets)
                .totalRequests(totalRequests)
                .totalCompletedRequests(totalCompletedRequests)
                .totalReviews(totalReviews)
                .todayNewRequests(todayNewRequests)
                .requestStatusCount(requestStatusCount)
                .petSpeciesCount(petSpeciesCount)
                .topRatedUsers(topRatedUsers)
                .build();
    }

    public StatsDTO.TrendStats getTrendStats(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        if (startDate.isAfter(endDate)) {
            LocalDate temp = startDate;
            startDate = endDate;
            endDate = temp;
        }

        List<LocalDate> dateRange = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            dateRange.add(current);
            current = current.plusDays(1);
        }

        Map<LocalDate, Long> dailyRequestsMap = new HashMap<>();
        Map<LocalDate, Long> dailyCompletedMap = new HashMap<>();
        Map<LocalDate, Long> dailyUsersMap = new HashMap<>();
        Map<LocalDate, Long> dailyReviewsMap = new HashMap<>();

        for (FosterRequest req : requestRepository.findAll()) {
            LocalDateTime createdAt = req.getCreatedAt();
            if (createdAt != null) {
                LocalDate date = createdAt.toLocalDate();
                if (!date.isBefore(startDate) && !date.isAfter(endDate)) {
                    dailyRequestsMap.merge(date, 1L, Long::sum);
                    if (req.getStatus() == FosterRequest.Status.Completed) {
                        dailyCompletedMap.merge(date, 1L, Long::sum);
                    }
                }
            }
        }

        for (User user : userRepository.findAll()) {
            LocalDateTime createdAt = user.getCreatedAt();
            if (createdAt != null) {
                LocalDate date = createdAt.toLocalDate();
                if (!date.isBefore(startDate) && !date.isAfter(endDate)) {
                    dailyUsersMap.merge(date, 1L, Long::sum);
                }
            }
        }

        for (var review : reviewRepository.findAll()) {
            LocalDateTime createdAt = review.getCreatedAt();
            if (createdAt != null) {
                LocalDate date = createdAt.toLocalDate();
                if (!date.isBefore(startDate) && !date.isAfter(endDate)) {
                    dailyReviewsMap.merge(date, 1L, Long::sum);
                }
            }
        }

        List<StatsDTO.DailyCount> dailyRequests = dateRange.stream()
                .map(d -> StatsDTO.DailyCount.builder()
                        .date(d.format(DATE_FORMATTER))
                        .count(dailyRequestsMap.getOrDefault(d, 0L))
                        .build())
                .toList();

        List<StatsDTO.DailyCount> dailyCompletedRequests = dateRange.stream()
                .map(d -> StatsDTO.DailyCount.builder()
                        .date(d.format(DATE_FORMATTER))
                        .count(dailyCompletedMap.getOrDefault(d, 0L))
                        .build())
                .toList();

        List<StatsDTO.DailyCount> dailyUsers = dateRange.stream()
                .map(d -> StatsDTO.DailyCount.builder()
                        .date(d.format(DATE_FORMATTER))
                        .count(dailyUsersMap.getOrDefault(d, 0L))
                        .build())
                .toList();

        List<StatsDTO.DailyCount> dailyReviews = dateRange.stream()
                .map(d -> StatsDTO.DailyCount.builder()
                        .date(d.format(DATE_FORMATTER))
                        .count(dailyReviewsMap.getOrDefault(d, 0L))
                        .build())
                .toList();

        return StatsDTO.TrendStats.builder()
                .startDate(startDate.format(DATE_FORMATTER))
                .endDate(endDate.format(DATE_FORMATTER))
                .dailyRequests(dailyRequests)
                .dailyCompletedRequests(dailyCompletedRequests)
                .dailyUsers(dailyUsers)
                .dailyReviews(dailyReviews)
                .build();
    }

    private List<StatsDTO.TopUser> calculateTopRatedUsers(int limit) {
        List<StatsDTO.TopUser> allUsers = new ArrayList<>();

        for (User user : userRepository.findAll()) {
            long reviewCount = reviewRepository.countByRevieweeId(user.getId());
            if (reviewCount == 0) continue;

            Double avgRating = reviewRepository.findAverageRatingByRevieweeId(user.getId());
            Double avgResponsibility = reviewRepository.findAverageResponsibilityByRevieweeId(user.getId());
            Double avgCommunication = reviewRepository.findAverageCommunicationByRevieweeId(user.getId());
            Double avgPetCondition = reviewRepository.findAveragePetConditionByRevieweeId(user.getId());

            if (avgRating == null) avgRating = 0.0;
            if (avgResponsibility == null) avgResponsibility = 0.0;
            if (avgCommunication == null) avgCommunication = 0.0;
            if (avgPetCondition == null) avgPetCondition = 0.0;

            allUsers.add(StatsDTO.TopUser.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .averageRating(Math.round(avgRating * 100.0) / 100.0)
                    .averageResponsibility(Math.round(avgResponsibility * 100.0) / 100.0)
                    .averageCommunication(Math.round(avgCommunication * 100.0) / 100.0)
                    .averagePetCondition(Math.round(avgPetCondition * 100.0) / 100.0)
                    .reviewCount(reviewCount)
                    .build());
        }

        allUsers.sort((u1, u2) -> {
            int ratingCompare = Double.compare(u2.getAverageRating(), u1.getAverageRating());
            if (ratingCompare != 0) return ratingCompare;
            return Long.compare(u2.getReviewCount(), u1.getReviewCount());
        });

        return allUsers.stream().limit(limit).toList();
    }
}
