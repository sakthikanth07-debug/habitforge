package com.personalhabitstreaktracker.habitforge.service;

import com.personalhabitstreaktracker.habitforge.entity.Habit;
import com.personalhabitstreaktracker.habitforge.entity.HabitFrequency;
import com.personalhabitstreaktracker.habitforge.entity.HabitReminder;
import com.personalhabitstreaktracker.habitforge.repository.CompletionLogRepo;
import com.personalhabitstreaktracker.habitforge.repository.HabitReminderRepo;
import com.personalhabitstreaktracker.habitforge.repository.HabitTrackerRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class HabitReminderService {

    private static final Logger logger = LoggerFactory.getLogger(HabitReminderService.class);

    private final HabitTrackerRepo habitTrackerRepo;
    private final CompletionLogRepo completionLogRepo;
    private final HabitReminderRepo habitReminderRepo;
    private final PushNotificationService pushNotificationService;
    private final DesktopNotificationService desktopNotificationService;
    private final ZoneId zoneId;

    public HabitReminderService(HabitTrackerRepo habitTrackerRepo,
                                CompletionLogRepo completionLogRepo,
                                HabitReminderRepo habitReminderRepo,
                                PushNotificationService pushNotificationService,
                                DesktopNotificationService desktopNotificationService,
                                ZoneId zoneId) {
        this.habitTrackerRepo = habitTrackerRepo;
        this.completionLogRepo = completionLogRepo;
        this.habitReminderRepo = habitReminderRepo;
        this.pushNotificationService = pushNotificationService;
        this.desktopNotificationService = desktopNotificationService;
        this.zoneId = zoneId;
    }

    @Transactional
    public void checkDueReminders() {
        LocalDateTime now = LocalDateTime.now(zoneId);
        for (Habit habit : habitTrackerRepo.findByReminderTimeIsNotNull()) {
            try {
                createReminderIfDue(habit, now);
            } catch (RuntimeException exception) {
                logger.warn("Could not process reminder for habit {}", habit.getId(), exception);
            }
        }
    }

    @Transactional
    public void createReminderIfDue(Habit habit, LocalDateTime now) {
        LocalDate date = now.toLocalDate();
        if (!isScheduledOn(habit, date)) {
            return;
        }

        if (habit.getReminderTime() == null || habit.getReminderTime().isBlank()) {
            return;
        }

        LocalTime reminderTime;
        try {
            reminderTime = LocalTime.parse(habit.getReminderTime());
        } catch (DateTimeParseException exception) {
            logger.warn("Ignoring invalid reminder time for habit {}", habit.getId());
            return;
        }

        if (now.toLocalTime().isBefore(reminderTime)) {
            return;
        }

        if (!completionLogRepo.findByHabitAndCompletionDate(habit, date).isEmpty()) {
            logger.info("[REMINDER] Habit completed today: {}", habit.getName());
            return;
        }

        if (habitReminderRepo.existsByHabitAndReminderDate(habit, date)) {
            return;
        }

        String message = "You haven't completed " + habit.getName() + " today.";
        HabitReminder reminder = habitReminderRepo.save(new HabitReminder(habit, date, reminderTime, message, now));
        logger.info("[REMINDER] Missed habit detected: {}", habit.getName());
        desktopNotificationService.sendReminderNotification(habit.getName(), message);
        pushNotificationService.send(reminder);
    }

    @Transactional(readOnly = true)
    public List<HabitReminder> getPendingReminders(LocalDate date) {
        return habitReminderRepo.findPendingByDate(date);
    }

    @Transactional
    public void resolveReminder(Habit habit, LocalDate date) {
        habitReminderRepo.deleteByHabitAndReminderDate(habit, date);
    }

    @Transactional
    public void deleteReminders(Habit habit) {
        habitReminderRepo.deleteByHabit(habit);
    }

    private boolean isScheduledOn(Habit habit, LocalDate date) {
        if (habit.getFrequency() == HabitFrequency.DAILY) {
            return true;
        }
        if (habit.getFrequency() != HabitFrequency.SPECIFIC_WEEKDAYS
                || habit.getWeekdays() == null
                || habit.getWeekdays().isBlank()) {
            return false;
        }
        return List.of(habit.getWeekdays().split(",")).stream()
                .map(String::trim)
                .anyMatch(day -> day.equalsIgnoreCase(date.getDayOfWeek().name()));
    }
}