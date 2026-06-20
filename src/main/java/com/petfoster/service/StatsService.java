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

    public StatsDTO.PopularBreedStats getPopularBreedStats(int topN) {
        List<FosterRequest> allRequests = requestRepository.findAll();
        Map<Long, Pet> petMap = new HashMap<>();
        for (Pet pet : petRepository.findAll()) {
            petMap.put(pet.getId(), pet);
        }

        Map<String, long[]> breedStats = new HashMap<>();
        long totalRequests = 0;

        for (FosterRequest req : allRequests) {
            Pet pet = petMap.get(req.getPetId());
            if (pet == null) continue;

            String breed = pet.getBreed();
            if (breed == null || breed.isEmpty()) {
                breed = "未知品种";
            }
            String species = pet.getSpecies();

            breedStats.computeIfAbsent(breed, k -> new long[]{0, 0});
            breedStats.get(breed)[0]++;
            breedStats.get(breed)[1] = species != null ? species.hashCode() : 0;
            totalRequests++;
        }

        List<StatsDTO.PetBreedRank> breedRanks = new ArrayList<>();
        for (Map.Entry<String, long[]> entry : breedStats.entrySet()) {
            String breed = entry.getKey();
            long count = entry.getValue()[0];
            String species = "";

            Pet samplePet = petMap.values().stream()
                    .filter(p -> breed.equals(p.getBreed()) || ("未知品种".equals(breed) && (p.getBreed() == null || p.getBreed().isEmpty())))
                    .findFirst()
                    .orElse(null);
            if (samplePet != null) {
                species = samplePet.getSpecies();
            }

            double percentage = totalRequests > 0 ? Math.round(count * 10000.0 / totalRequests) / 100.0 : 0.0;

            breedRanks.add(StatsDTO.PetBreedRank.builder()
                    .breed(breed)
                    .species(species)
                    .requestCount(count)
                    .percentage(percentage)
                    .build());
        }

        breedRanks.sort((b1, b2) -> {
            int countCompare = Long.compare(b2.getRequestCount(), b1.getRequestCount());
            if (countCompare != 0) return countCompare;
            return b1.getBreed().compareTo(b2.getBreed());
        });

        if (topN > 0 && breedRanks.size() > topN) {
            breedRanks = breedRanks.subList(0, topN);
        }

        return StatsDTO.PopularBreedStats.builder()
                .topBreeds(breedRanks)
                .totalRequests(totalRequests)
                .totalBreeds(breedStats.size())
                .build();
    }

    public StatsDTO.FosterDurationStats getFosterDurationStats() {
        List<FosterRequest> completedRequests = requestRepository.findAll().stream()
                .filter(r -> r.getStatus() == FosterRequest.Status.Completed)
                .toList();

        if (completedRequests.isEmpty()) {
            return StatsDTO.FosterDurationStats.builder()
                    .averageDays(0)
                    .medianDays(0)
                    .shortestDays(0)
                    .longestDays(0)
                    .totalCompletedRequests(0)
                    .averageBySpecies(new HashMap<>())
                    .averageByBreed(new HashMap<>())
                    .build();
        }

        Map<Long, Pet> petMap = new HashMap<>();
        for (Pet pet : petRepository.findAll()) {
            petMap.put(pet.getId(), pet);
        }

        List<Long> durations = new ArrayList<>();
        Map<String, List<Long>> durationsBySpecies = new HashMap<>();
        Map<String, List<Long>> durationsByBreed = new HashMap<>();

        for (FosterRequest req : completedRequests) {
            if (req.getStartDate() == null || req.getEndDate() == null) continue;

            long days = java.time.temporal.ChronoUnit.DAYS.between(req.getStartDate(), req.getEndDate()) + 1;
            durations.add(days);

            Pet pet = petMap.get(req.getPetId());
            if (pet != null) {
                String species = pet.getSpecies();
                if (species != null && !species.isEmpty()) {
                    durationsBySpecies.computeIfAbsent(species, k -> new ArrayList<>()).add(days);
                }

                String breed = pet.getBreed();
                if (breed == null || breed.isEmpty()) {
                    breed = "未知品种";
                }
                durationsByBreed.computeIfAbsent(breed, k -> new ArrayList<>()).add(days);
            }
        }

        Collections.sort(durations);

        double averageDays = durations.stream().mapToLong(Long::longValue).average().orElse(0.0);
        averageDays = Math.round(averageDays * 100.0) / 100.0;

        double medianDays;
        int size = durations.size();
        if (size % 2 == 0) {
            medianDays = (durations.get(size / 2 - 1) + durations.get(size / 2)) / 2.0;
        } else {
            medianDays = durations.get(size / 2);
        }
        medianDays = Math.round(medianDays * 100.0) / 100.0;

        long shortestDays = durations.get(0);
        long longestDays = durations.get(durations.size() - 1);

        Map<String, Double> averageBySpecies = new HashMap<>();
        for (Map.Entry<String, List<Long>> entry : durationsBySpecies.entrySet()) {
            double avg = entry.getValue().stream().mapToLong(Long::longValue).average().orElse(0.0);
            averageBySpecies.put(entry.getKey(), Math.round(avg * 100.0) / 100.0);
        }

        Map<String, Double> averageByBreed = new HashMap<>();
        for (Map.Entry<String, List<Long>> entry : durationsByBreed.entrySet()) {
            double avg = entry.getValue().stream().mapToLong(Long::longValue).average().orElse(0.0);
            averageByBreed.put(entry.getKey(), Math.round(avg * 100.0) / 100.0);
        }

        return StatsDTO.FosterDurationStats.builder()
                .averageDays(averageDays)
                .medianDays(medianDays)
                .shortestDays(shortestDays)
                .longestDays(longestDays)
                .totalCompletedRequests(completedRequests.size())
                .averageBySpecies(averageBySpecies)
                .averageByBreed(averageByBreed)
                .build();
    }

    public StatsDTO.UserFosterStats getUserFosterStats(Long userId) {
        long publishedRequests = requestRepository.countByOwnerId(userId);
        long completedFosters = requestRepository.countCompletedByUserId(userId);
        long receivedReviews = reviewRepository.countByRevieweeId(userId);

        Double avgRating = reviewRepository.findAverageRatingByRevieweeId(userId);
        double averageRating = avgRating != null ? Math.round(avgRating * 100.0) / 100.0 : 0.0;

        return StatsDTO.UserFosterStats.builder()
                .publishedRequests(publishedRequests)
                .completedFosters(completedFosters)
                .receivedReviews(receivedReviews)
                .averageRating(averageRating)
                .build();
    }
}
