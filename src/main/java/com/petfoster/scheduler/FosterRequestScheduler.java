package com.petfoster.scheduler;

import com.petfoster.service.FosterRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FosterRequestScheduler {

    private final FosterRequestService fosterRequestService;

    @Value("${foster.request.timeout-days:7}")
    private int timeoutDays;

    @Scheduled(cron = "${foster.request.timeout-cron:0 0 2 * * ?}")
    public void cancelExpiredApprovedRequests() {
        log.info("开始执行寄养申请超时自动取消检查，超时阈值：{} 天", timeoutDays);
        int cancelledCount = fosterRequestService.cancelExpiredApprovedRequests(timeoutDays);
        if (cancelledCount > 0) {
            log.info("寄养申请超时自动取消完成，共取消 {} 条申请", cancelledCount);
        } else {
            log.info("未发现需要超时取消的寄养申请");
        }
    }
}
