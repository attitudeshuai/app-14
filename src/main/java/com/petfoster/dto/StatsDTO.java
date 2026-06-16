package com.petfoster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

public class StatsDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverviewStats {
        private long totalUsers;
        private long totalPets;
        private long totalRequests;
        private long totalCompletedRequests;
        private long totalReviews;
        private long todayNewRequests;
        private Map<String, Long> requestStatusCount;
        private Map<String, Long> petSpeciesCount;
        private List<TopUser> topRatedUsers;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopUser {
        private Long userId;
        private String username;
        private Double averageRating;
        private long reviewCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendStats {
        private String startDate;
        private String endDate;
        private List<DailyCount> dailyRequests;
        private List<DailyCount> dailyCompletedRequests;
        private List<DailyCount> dailyUsers;
        private List<DailyCount> dailyReviews;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyCount {
        private String date;
        private long count;
    }
}
