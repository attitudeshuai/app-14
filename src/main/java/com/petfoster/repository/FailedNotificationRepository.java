package com.petfoster.repository;

import com.petfoster.entity.FailedNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FailedNotificationRepository extends JpaRepository<FailedNotification, Long> {

    List<FailedNotification> findByStatusAndNextRetryAtBefore(
            FailedNotification.Status status, LocalDateTime now);

    List<FailedNotification> findByStatus(FailedNotification.Status status);
}
