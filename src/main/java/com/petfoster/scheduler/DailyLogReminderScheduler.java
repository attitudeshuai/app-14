package com.petfoster.scheduler;

import com.petfoster.service.DailyLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyLogReminderScheduler {

    private final DailyLogService dailyLogService;

    @Value("${foster.daily-log.reminder-cron:0 0 20 * * ?}")
    private String reminderCron;

    @Value("${foster.daily-log.missed-days:2}")
    private int missedDays;

    @Value("${foster.daily-log.missed-reminder-cron:0 0 21 * * ?}")
    private String missedReminderCron;

    @Scheduled(cron = "${foster.daily-log.reminder-cron:0 0 20 * * ?}")
    public void sendDailyLogReminders() {
        log.info("开始执行每日日报提醒任务");
        int count = dailyLogService.sendDailyLogReminders();
        if (count > 0) {
            log.info("每日日报提醒执行完成，共发送 {} 条提醒", count);
        } else {
            log.info("每日日报提醒执行完成，无需发送提醒");
        }
    }

    @Scheduled(cron = "${foster.daily-log.missed-reminder-cron:0 0 21 * * ?}")
    public void sendMissedDailyLogReminders() {
        log.info("开始执行连续未写日报提醒任务，连续未写天数阈值：{} 天", missedDays);
        int count = dailyLogService.sendMissedDailyLogReminders(missedDays);
        if (count > 0) {
            log.info("连续未写日报提醒执行完成，共发送 {} 条提醒", count);
        } else {
            log.info("连续未写日报提醒执行完成，无需发送提醒");
        }
    }
}
