package com.petfoster.service;

import com.petfoster.dto.ReputationDTO;
import com.petfoster.entity.User;
import com.petfoster.repository.FosterRequestRepository;
import com.petfoster.repository.FosterReviewRepository;
import com.petfoster.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReputationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FosterRequestRepository requestRepository;

    @Mock
    private FosterReviewRepository reviewRepository;

    @InjectMocks
    private ReputationService reputationService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@test.com")
                .createdAt(LocalDateTime.now().minusDays(100))
                .build();
    }

    @Test
    @DisplayName("计算信誉分 - 新用户基础分")
    void testCalculateReputation_NewUser() {
        testUser.setCreatedAt(LocalDateTime.now().minusDays(15));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(requestRepository.countCompletedByUserId(1L)).thenReturn(0L);
        when(reviewRepository.findAverageRatingByRevieweeId(1L)).thenReturn(null);
        when(reviewRepository.countByRevieweeId(1L)).thenReturn(0L);
        when(requestRepository.countCancelledByUserId(1L)).thenReturn(0L);

        ReputationDTO result = reputationService.calculateReputation(1L);

        assertNotNull(result);
        assertEquals(25 + 5 + 0 + 10 - 0, result.getTotalScore());
        assertEquals(40, result.getTotalScore());
        assertEquals("较差", result.getLevel());
        assertEquals(5, result.getRegisterMonthsScore());
        assertEquals(0, result.getCompletedFostersScore());
        assertEquals(10, result.getAverageRatingScore());
        assertEquals(0, result.getDefaultDeduction());
        assertEquals(15, result.getRegisterDays());
        assertEquals(0, result.getCompletedFosters());
        assertEquals(0, result.getDefaultCount());
    }

    @Test
    @DisplayName("计算信誉分 - 老用户高分")
    void testCalculateReputation_ExcellentUser() {
        testUser.setCreatedAt(LocalDateTime.now().minusDays(200));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(requestRepository.countCompletedByUserId(1L)).thenReturn(15L);
        when(reviewRepository.findAverageRatingByRevieweeId(1L)).thenReturn(4.8);
        when(reviewRepository.countByRevieweeId(1L)).thenReturn(12L);
        when(requestRepository.countCancelledByUserId(1L)).thenReturn(0L);

        ReputationDTO result = reputationService.calculateReputation(1L);

        assertNotNull(result);
        assertEquals(25 + 20 + 25 + 30 - 0, result.getTotalScore());
        assertEquals(100, result.getTotalScore());
        assertEquals("优秀", result.getLevel());
        assertEquals(20, result.getRegisterMonthsScore());
        assertEquals(25, result.getCompletedFostersScore());
        assertEquals(30, result.getAverageRatingScore());
        assertEquals(4.8, result.getAverageRating());
        assertEquals(12, result.getReviewCount());
    }

    @Test
    @DisplayName("计算信誉分 - 有违约记录")
    void testCalculateReputation_WithDefaults() {
        testUser.setCreatedAt(LocalDateTime.now().minusDays(100));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(requestRepository.countCompletedByUserId(1L)).thenReturn(5L);
        when(reviewRepository.findAverageRatingByRevieweeId(1L)).thenReturn(3.5);
        when(reviewRepository.countByRevieweeId(1L)).thenReturn(5L);
        when(requestRepository.countCancelledByUserId(1L)).thenReturn(3L);

        ReputationDTO result = reputationService.calculateReputation(1L);

        assertNotNull(result);
        int expectedScore = 25 + 15 + 18 + 22 - 45;
        assertEquals(expectedScore, result.getTotalScore());
        assertEquals(35, result.getTotalScore());
        assertEquals("很差", result.getLevel());
        assertEquals(45, result.getDefaultDeduction());
        assertEquals(3, result.getDefaultCount());
    }

    @Test
    @DisplayName("计算信誉分 - 最低分不低于0")
    void testCalculateReputation_MinScoreZero() {
        testUser.setCreatedAt(LocalDateTime.now().minusDays(10));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(requestRepository.countCompletedByUserId(1L)).thenReturn(0L);
        when(reviewRepository.findAverageRatingByRevieweeId(1L)).thenReturn(1.0);
        when(reviewRepository.countByRevieweeId(1L)).thenReturn(5L);
        when(requestRepository.countCancelledByUserId(1L)).thenReturn(10L);

        ReputationDTO result = reputationService.calculateReputation(1L);

        assertNotNull(result);
        assertEquals(0, result.getTotalScore());
        assertEquals("很差", result.getLevel());
    }

    @Test
    @DisplayName("计算信誉分 - 最高分不超过100")
    void testCalculateReputation_MaxScoreHundred() {
        testUser.setCreatedAt(LocalDateTime.now().minusDays(365));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(requestRepository.countCompletedByUserId(1L)).thenReturn(50L);
        when(reviewRepository.findAverageRatingByRevieweeId(1L)).thenReturn(5.0);
        when(reviewRepository.countByRevieweeId(1L)).thenReturn(50L);
        when(requestRepository.countCancelledByUserId(1L)).thenReturn(0L);

        ReputationDTO result = reputationService.calculateReputation(1L);

        assertNotNull(result);
        assertEquals(100, result.getTotalScore());
        assertEquals("优秀", result.getLevel());
    }

    @Test
    @DisplayName("计算信誉分 - 用户不存在")
    void testCalculateReputation_UserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reputationService.calculateReputation(999L));

        assertEquals("用户不存在", exception.getMessage());
    }

    @Test
    @DisplayName("计算信誉分 - 中等评分用户")
    void testCalculateReputation_AverageUser() {
        testUser.setCreatedAt(LocalDateTime.now().minusDays(60));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(requestRepository.countCompletedByUserId(1L)).thenReturn(2L);
        when(reviewRepository.findAverageRatingByRevieweeId(1L)).thenReturn(3.2);
        when(reviewRepository.countByRevieweeId(1L)).thenReturn(2L);
        when(requestRepository.countCancelledByUserId(1L)).thenReturn(1L);

        ReputationDTO result = reputationService.calculateReputation(1L);

        assertNotNull(result);
        int expectedScore = 25 + 10 + 10 + 22 - 15;
        assertEquals(expectedScore, result.getTotalScore());
        assertEquals(52, result.getTotalScore());
        assertEquals("较差", result.getLevel());
    }

    @Test
    @DisplayName("计算信誉分 - 无评价用户")
    void testCalculateReputation_NoReviews() {
        testUser.setCreatedAt(LocalDateTime.now().minusDays(120));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(requestRepository.countCompletedByUserId(1L)).thenReturn(3L);
        when(reviewRepository.findAverageRatingByRevieweeId(1L)).thenReturn(null);
        when(reviewRepository.countByRevieweeId(1L)).thenReturn(0L);
        when(requestRepository.countCancelledByUserId(1L)).thenReturn(0L);

        ReputationDTO result = reputationService.calculateReputation(1L);

        assertNotNull(result);
        assertEquals(25 + 15 + 10 + 10 - 0, result.getTotalScore());
        assertEquals(60, result.getTotalScore());
        assertEquals("中等", result.getLevel());
        assertEquals(0.0, result.getAverageRating());
        assertEquals(0, result.getReviewCount());
    }
}
