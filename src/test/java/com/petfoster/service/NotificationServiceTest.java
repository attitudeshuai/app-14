package com.petfoster.service;

import com.petfoster.entity.FailedNotification;
import com.petfoster.entity.Notification;
import com.petfoster.repository.FailedNotificationRepository;
import com.petfoster.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private FailedNotificationRepository failedNotificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Notification testNotification;
    private FailedNotification pendingFailed;
    private FailedNotification deadFailed;

    @BeforeEach
    void setUp() {
        testNotification = Notification.builder()
                .id(1L)
                .userId(100L)
                .type(Notification.Type.FOSTER_REQUEST_STATUS)
                .title("寄养申请状态变更")
                .content("测试内容")
                .relatedId(1L)
                .relatedType(Notification.RelatedType.FOSTER_REQUEST)
                .isRead(false)
                .build();

        pendingFailed = FailedNotification.builder()
                .id(1L)
                .userId(100L)
                .type(Notification.Type.DAILY_LOG_CREATED)
                .title("新的寄养日报已发布")
                .content("补偿测试")
                .relatedId(2L)
                .relatedType(Notification.RelatedType.DAILY_LOG)
                .retryCount(1)
                .maxRetries(3)
                .status(FailedNotification.Status.PENDING)
                .nextRetryAt(LocalDateTime.now().minusMinutes(1))
                .build();

        deadFailed = FailedNotification.builder()
                .id(2L)
                .userId(200L)
                .type(Notification.Type.FOSTER_REQUEST_CREATED)
                .title("收到新的寄养申请")
                .content("死亡消息测试")
                .relatedId(3L)
                .relatedType(Notification.RelatedType.FOSTER_REQUEST)
                .retryCount(3)
                .maxRetries(3)
                .status(FailedNotification.Status.DEAD)
                .build();
    }

    @Test
    @DisplayName("发送通知 - 成功")
    void testSendNotification_Success() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        assertDoesNotThrow(() -> notificationService.sendNotification(
                100L,
                Notification.Type.FOSTER_REQUEST_STATUS,
                "寄养申请状态变更",
                "测试内容",
                1L,
                Notification.RelatedType.FOSTER_REQUEST
        ));

        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("定时重试 - PENDING消息重试成功后标记为RESOLVED")
    void testRetryFailedNotifications_PendingRetrySuccess() {
        when(failedNotificationRepository.findByStatusAndNextRetryAtBefore(
                eq(FailedNotification.Status.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(pendingFailed));
        when(failedNotificationRepository.save(any(FailedNotification.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        notificationService.retryFailedNotifications();

        verify(notificationRepository).save(any(Notification.class));
        assertEquals(FailedNotification.Status.RESOLVED, pendingFailed.getStatus());
    }

    @Test
    @DisplayName("定时重试 - PENDING消息重试失败后递增retryCount")
    void testRetryFailedNotifications_PendingRetryFailIncrementCount() {
        when(failedNotificationRepository.findByStatusAndNextRetryAtBefore(
                eq(FailedNotification.Status.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(pendingFailed));
        when(failedNotificationRepository.save(any(FailedNotification.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(notificationRepository.save(any(Notification.class)))
                .thenThrow(new RuntimeException("DB连接异常"));

        notificationService.retryFailedNotifications();

        assertEquals(2, pendingFailed.getRetryCount());
        assertEquals(FailedNotification.Status.PENDING, pendingFailed.getStatus());
        assertNotNull(pendingFailed.getNextRetryAt());
        assertNotNull(pendingFailed.getLastError());
    }

    @Test
    @DisplayName("定时重试 - PENDING消息超过maxRetries后标记为DEAD")
    void testRetryFailedNotifications_ExceedMaxRetries() {
        pendingFailed.setRetryCount(2);
        when(failedNotificationRepository.findByStatusAndNextRetryAtBefore(
                eq(FailedNotification.Status.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(pendingFailed));
        when(failedNotificationRepository.save(any(FailedNotification.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(notificationRepository.save(any(Notification.class)))
                .thenThrow(new RuntimeException("DB连接异常"));

        notificationService.retryFailedNotifications();

        assertEquals(3, pendingFailed.getRetryCount());
        assertEquals(FailedNotification.Status.DEAD, pendingFailed.getStatus());
    }

    @Test
    @DisplayName("定时重试 - 无待重试消息时不执行任何操作")
    void testRetryFailedNotifications_NoPendingMessages() {
        when(failedNotificationRepository.findByStatusAndNextRetryAtBefore(
                eq(FailedNotification.Status.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of());

        notificationService.retryFailedNotifications();

        verify(notificationRepository, never()).save(any());
        verify(failedNotificationRepository, never()).save(any(FailedNotification.class));
    }

    @Test
    @DisplayName("手动重试DEAD消息 - 成功恢复后标记为RESOLVED")
    void testRetryAllDeadNotifications_Success() {
        when(failedNotificationRepository.findByStatus(FailedNotification.Status.DEAD))
                .thenReturn(List.of(deadFailed));
        when(failedNotificationRepository.save(any(FailedNotification.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        int recovered = notificationService.retryAllDeadNotifications();

        assertEquals(1, recovered);
        assertEquals(FailedNotification.Status.RESOLVED, deadFailed.getStatus());
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("手动重试DEAD消息 - 重试仍失败时保留DEAD状态并递增计数")
    void testRetryAllDeadNotifications_StillFails() {
        when(failedNotificationRepository.findByStatus(FailedNotification.Status.DEAD))
                .thenReturn(List.of(deadFailed));
        when(failedNotificationRepository.save(any(FailedNotification.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(notificationRepository.save(any(Notification.class)))
                .thenThrow(new RuntimeException("DB仍然不可用"));

        int recovered = notificationService.retryAllDeadNotifications();

        assertEquals(0, recovered);
        assertEquals(4, deadFailed.getRetryCount());
        assertNotNull(deadFailed.getLastError());
    }

    @Test
    @DisplayName("手动重试DEAD消息 - 无DEAD消息时返回0")
    void testRetryAllDeadNotifications_NoDeadMessages() {
        when(failedNotificationRepository.findByStatus(FailedNotification.Status.DEAD))
                .thenReturn(List.of());

        int recovered = notificationService.retryAllDeadNotifications();

        assertEquals(0, recovered);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("手动重试DEAD消息 - 多条DEAD消息部分成功部分失败")
    void testRetryAllDeadNotifications_PartialSuccess() {
        FailedNotification deadFailed2 = FailedNotification.builder()
                .id(3L)
                .userId(300L)
                .type(Notification.Type.DAILY_LOG_UPDATED)
                .title("寄养日报已更新")
                .content("部分成功测试")
                .relatedId(4L)
                .relatedType(Notification.RelatedType.DAILY_LOG)
                .retryCount(3)
                .maxRetries(3)
                .status(FailedNotification.Status.DEAD)
                .build();

        when(failedNotificationRepository.findByStatus(FailedNotification.Status.DEAD))
                .thenReturn(List.of(deadFailed, deadFailed2));
        when(failedNotificationRepository.save(any(FailedNotification.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(testNotification)
                .thenThrow(new RuntimeException("第二次仍然失败"));

        int recovered = notificationService.retryAllDeadNotifications();

        assertEquals(1, recovered);
        assertEquals(FailedNotification.Status.RESOLVED, deadFailed.getStatus());
        assertEquals(4, deadFailed2.getRetryCount());
    }
}
