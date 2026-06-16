package com.petfoster.service;

import com.petfoster.common.BusinessException;
import com.petfoster.common.PageResponse;
import com.petfoster.dto.DailyLogDTO;
import com.petfoster.entity.FosterDailyLog;
import com.petfoster.entity.FosterRequest;
import com.petfoster.entity.User;
import com.petfoster.repository.FosterDailyLogRepository;
import com.petfoster.repository.FosterRequestRepository;
import com.petfoster.repository.UserRepository;
import com.petfoster.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyLogService {

    private final FosterDailyLogRepository logRepository;
    private final FosterRequestRepository requestRepository;
    private final UserRepository userRepository;

    public PageResponse<DailyLogDTO.LogResponse> getLogs(
            int page, int size, String sort,
            Long requestId, Long fostererId, LocalDate startDate, LocalDate endDate) {

        Sort sortObj = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<FosterDailyLog> logPage = logRepository.searchLogs(
                requestId, fostererId, startDate, endDate, pageable);

        return buildPageResponse(logPage);
    }

    public DailyLogDTO.LogResponse getLogById(Long id) {
        FosterDailyLog log = logRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("寄养日报不存在"));
        User fosterer = userRepository.findById(log.getFostererId()).orElse(null);
        return EntityMapper.toDailyLogResponse(log, fosterer);
    }

    @Transactional
    public DailyLogDTO.LogResponse createLog(Long userId, DailyLogDTO.CreateLogRequest req) {
        FosterRequest request = requestRepository.findById(req.getRequestId())
                .orElseThrow(() -> BusinessException.notFound("寄养申请不存在"));

        if (request.getStatus() != FosterRequest.Status.InProgress
                && request.getStatus() != FosterRequest.Status.Approved) {
            throw BusinessException.badRequest("只有已批准或进行中的寄养申请才能创建日报");
        }

        boolean isFosterer = request.getFostererId() != null
                && request.getFostererId().equals(userId);
        if (!isFosterer) {
            throw BusinessException.forbidden("只有寄养人才能创建日报");
        }

        if (req.getLogDate().isBefore(request.getStartDate())
                || req.getLogDate().isAfter(request.getEndDate())) {
            throw BusinessException.badRequest("日志日期必须在寄养期间内");
        }

        if (logRepository.existsByRequestIdAndLogDate(req.getRequestId(), req.getLogDate())) {
            throw BusinessException.badRequest("该日期的日报已存在，请使用更新接口");
        }

        FosterDailyLog log = FosterDailyLog.builder()
                .requestId(req.getRequestId())
                .fostererId(userId)
                .logDate(req.getLogDate())
                .food(req.getFood())
                .mood(req.getMood())
                .photos(req.getPhotos())
                .note(req.getNote())
                .build();

        log = logRepository.save(log);
        log_info("寄养日报创建成功: logId={}, requestId={}", log.getId(), req.getRequestId());

        User fosterer = userRepository.findById(userId).orElse(null);
        return EntityMapper.toDailyLogResponse(log, fosterer);
    }

    private void log_info(String msg, Object... args) {
        // 避免变量名冲突
        log.info(msg, args);
    }

    @Transactional
    public DailyLogDTO.LogResponse updateLog(Long userId, Long logId, DailyLogDTO.UpdateLogRequest req) {
        FosterDailyLog log = logRepository.findById(logId)
                .orElseThrow(() -> BusinessException.notFound("寄养日报不存在"));

        if (!log.getFostererId().equals(userId)) {
            throw BusinessException.forbidden("无权限修改此日报");
        }

        if (req.getLogDate() != null) {
            FosterRequest request = requestRepository.findById(log.getRequestId())
                    .orElseThrow(() -> BusinessException.notFound("寄养申请不存在"));
            if (req.getLogDate().isBefore(request.getStartDate())
                    || req.getLogDate().isAfter(request.getEndDate())) {
                throw BusinessException.badRequest("日志日期必须在寄养期间内");
            }
            if (!req.getLogDate().equals(log.getLogDate())
                    && logRepository.existsByRequestIdAndLogDate(log.getRequestId(), req.getLogDate())) {
                throw BusinessException.badRequest("该日期的日报已存在");
            }
            log.setLogDate(req.getLogDate());
        }

        if (req.getFood() != null) {
            log.setFood(req.getFood());
        }
        if (req.getMood() != null) {
            log.setMood(req.getMood());
        }
        if (req.getPhotos() != null) {
            log.setPhotos(req.getPhotos());
        }
        if (req.getNote() != null) {
            log.setNote(req.getNote());
        }

        log = logRepository.save(log);
        FosterDailyLog finalLog = log;
        log_info("寄养日报更新成功: logId={}", logId);

        User fosterer = userRepository.findById(log.getFostererId()).orElse(null);
        return EntityMapper.toDailyLogResponse(log, fosterer);
    }

    @Transactional
    public void deleteLog(Long userId, Long logId) {
        FosterDailyLog log = logRepository.findById(logId)
                .orElseThrow(() -> BusinessException.notFound("寄养日报不存在"));

        if (!log.getFostererId().equals(userId)) {
            throw BusinessException.forbidden("无权限删除此日报");
        }

        logRepository.delete(log);
        log_info("寄养日报删除成功: logId={}, userId={}", logId, userId);
    }

    private PageResponse<DailyLogDTO.LogResponse> buildPageResponse(Page<FosterDailyLog> page) {
        List<Long> fostererIds = page.getContent().stream()
                .map(FosterDailyLog::getFostererId).distinct().toList();
        Map<Long, User> userMap = userRepository.findAllById(fostererIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<DailyLogDTO.LogResponse> content = page.getContent().stream()
                .map(l -> EntityMapper.toDailyLogResponse(l, userMap.get(l.getFostererId())))
                .toList();

        return PageResponse.<DailyLogDTO.LogResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    private Sort parseSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.DESC, "logDate");
        }
        String[] parts = sort.split(",");
        String field = parts[0];
        Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1])
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        return switch (field) {
            case "logDate", "log_date" -> Sort.by(direction, "logDate");
            default -> Sort.by(Sort.Direction.DESC, "logDate");
        };
    }
}
