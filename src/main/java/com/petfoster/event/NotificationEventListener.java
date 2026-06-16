package com.petfoster.event;

import com.petfoster.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotificationEvent(NotificationEvent event) {
        for (NotificationEvent.NotificationEntry entry : event.getEntries()) {
            try {
                notificationService.sendNotification(
                        entry.getUserId(),
                        entry.getType(),
                        entry.getTitle(),
                        entry.getContent(),
                        entry.getRelatedId(),
                        entry.getRelatedType()
                );
            } catch (Exception e) {
                log.error("站内消息发送失败: userId={}, type={}, title={}, error={}",
                        entry.getUserId(), entry.getType(), entry.getTitle(), e.getMessage(), e);
            }
        }
    }
}
