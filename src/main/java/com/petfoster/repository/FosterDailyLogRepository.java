package com.petfoster.repository;

import com.petfoster.entity.FosterDailyLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FosterDailyLogRepository extends JpaRepository<FosterDailyLog, Long> {
    List<FosterDailyLog> findByRequestIdOrderByLogDateDesc(Long requestId);
    List<FosterDailyLog> findByFostererId(Long fostererId);

    @Query("SELECT l FROM FosterDailyLog l WHERE " +
           "(:requestId IS NULL OR l.requestId = :requestId) AND " +
           "(:fostererId IS NULL OR l.fostererId = :fostererId) AND " +
           "(:startDate IS NULL OR l.logDate >= :startDate) AND " +
           "(:endDate IS NULL OR l.logDate <= :endDate)")
    Page<FosterDailyLog> searchLogs(
            @Param("requestId") Long requestId,
            @Param("fostererId") Long fostererId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    boolean existsByRequestIdAndLogDate(Long requestId, LocalDate logDate);

    long countByRequestIdAndLogDateBetween(Long requestId, LocalDate startDate, LocalDate endDate);
}
