package com.petfoster.event;

import com.petfoster.entity.FailedNotification;
import com.petfoster.entity.Notification;
import com.petfoster.repository.FailedNotificationRepository;
import com.petfoster.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private FailedNotificationRepository failedNotificationRepository;

    @InjectMocks
    private NotificationEventListener listener;

    private NotificationEvent.NotificationEntry entry;

    @BeforeEach
    void setUp() {
        entry = NotificationEvent.entry(
                100L,
                Notification.Type.FOSTER_REQUEST_STATUS,
                "寄养申请状态变更",
                "测试通知内容",
                1L,
                Notification.RelatedType.FOSTER_REQUEST
        );
    }

    @Test
    @DisplayName("事件处理 - 单条通知成功发送")
    void testHandleEvent_SingleEntrySuccess() {
        NotificationEvent event = new NotificationEvent(List.of(entry));

        listener.handleNotificationEvent(event);

        verify(notificationService).sendNotification(
                100L,
                Notification.Type.FOSTER_REQUEST_STATUS,
                "寄养申请状态变更",
                "测试通知内容",
                1L,
                Notification.RelatedType.FOSTER_REQUEST
        );
        verify(failedNotificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("事件处理 - 多条通知全部成功发送")
    void testHandleEvent_MultipleEntriesSuccess() {
        NotificationEvent.NotificationEntry entry2 = NotificationEvent.entry(
                200L,
                Notification.Type.DAILY_LOG_CREATED,
                "新的寄养日报已发布",
                "日报内容",
                2L,
                Notification.RelatedType.DAILY_LOG
        );
        NotificationEvent event = new NotificationEvent(List.of(entry, entry2));

        listener.handleNotificationEvent(event);

        verify(notificationService, times(2)).sendNotification(any(), any(), any(), any(), any(), any());
        verify(failedNotificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("重试机制 - 第一次失败第二次成功")
    void testSendWithRetry_FirstFailSecondSuccess() {
        NotificationEvent event = new NotificationEvent(List.of(entry));

        doThrow(new RuntimeException("临时异常"))
                .doNothing()
                .when(notificationService).sendNotification(any(), any(), any(), any(), any(), any());

        listener.handleNotificationEvent(event);

        verify(notificationService, times(2)).sendNotification(any(), any(), any(), any(), any(), any());
        verify(failedNotificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("重试机制 - 3次全部失败后持久化到补偿表")
    void testSendWithRetry_AllRetriesFail_PersistToFailedTable() {
        NotificationEvent event = new NotificationEvent(List.of(entry));

        doThrow(new RuntimeException("持续异常"))
                .when(notificationService).sendNotification(any(), any(), any(), any(), any(), any());
        when(failedNotificationRepository.save(any(FailedNotification.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        listener.handleNotificationEvent(event);

        verify(notificationService, times(3)).sendNotification(any(), any(), any(), any(), any(), any());
        verify(failedNotificationRepository).save(argThat(failed ->
                failed.getUserId().equals(100L) &&
                failed.getType() == Notification.Type.FOSTER_REQUEST_STATUS &&
                failed.getStatus() == FailedNotification.Status.PENDING &&
                failed.getRetryCount() == 0
        ));
    }

    @Test
    @DisplayName("重试机制 - 多条通知中一条失败不影响其他")
    void testSendWithRetry_OneFailureDoesNotAffectOthers() {
        NotificationEvent.NotificationEntry successEntry = NotificationEvent.entry(
                200L,
                Notification.Type.DAILY_LOG_CREATED,
                "新的寄养日报已发布",
                "成功通知",
                2L,
                Notification.RelatedType.DAILY_LOG
        );

        NotificationEvent event = new NotificationEvent(List.of(successEntry, entry));

        doNothing()
                .when(notificationService).sendNotification(
                        eq(200L), any(), any(), any(), any(), any());
        doThrow(new RuntimeException("第二条失败"))
                .when(notificationService).sendNotification(
                        eq(100L), any(), any(), any(), any(), any());
        when(failedNotificationRepository.save(any(FailedNotification.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        listener.handleNotificationEvent(event);

        verify(notificationService).sendNotification(
                eq(200L), any(), any(), any(), any(), any());
        verify(notificationService, times(3)).sendNotification(
                eq(100L), any(), any(), any(), any(), any());
        verify(failedNotificationRepository).save(any(FailedNotification.class));
    }

    @Test
    @DisplayName("补偿持久化 - 保存的字段值正确")
    void testPersistFailedNotification_FieldsCorrect() {
        when(failedNotificationRepository.save(any(FailedNotification.class)))
                .thenAnswer(inv -> {
                    FailedNotification saved = inv.getArgument(0);
                    saved.setId(99L);
                    return saved;
                });

        Exception testException = new RuntimeException("数据库连接超时");

        listener.persistFailedNotification(entry, testException);

        verify(failedNotificationRepository).save(argThat(failed -> {
            assertEquals(100L, failed.getUserId());
            assertEquals(Notification.Type.FOSTER_REQUEST_STATUS, failed.getType());
            assertEquals("寄养申请状态变更", failed.getTitle());
            assertEquals("测试通知内容", failed.getContent());
            assertEquals(1L, failed.getRelatedId());
            assertEquals(Notification.RelatedType.FOSTER_REQUEST, failed.getRelatedType());
            assertEquals(0, failed.getRetryCount());
            assertEquals(3, failed.getMaxRetries());
            assertEquals(FailedNotification.Status.PENDING, failed.getStatus());
            assertNotNull(failed.getLastError());
            assertNotNull(failed.getNextRetryAt());
            return true;
        }));
    }
}
