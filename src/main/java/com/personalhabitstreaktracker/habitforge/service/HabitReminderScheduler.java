package com.personalhabitstreaktracker.habitforge.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HabitReminderScheduler {

    private static final Logger logger = LoggerFactory.getLogger(HabitReminderScheduler.class);

    private final HabitReminderService habitReminderService;

    public HabitReminderScheduler(HabitReminderService habitReminderService) {
        this.habitReminderService = habitReminderService;
    }

    @Scheduled(fixedRateString = "${habit.reminder.check-interval:10000}")
    public void checkReminders() {
        logger.info("[REMINDER] Checking habits...");
        habitReminderService.checkDueReminders();
    }
}