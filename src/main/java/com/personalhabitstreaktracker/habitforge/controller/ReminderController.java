package com.personalhabitstreaktracker.habitforge.controller;

import com.personalhabitstreaktracker.habitforge.entity.HabitReminder;
import com.personalhabitstreaktracker.habitforge.service.HabitReminderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.time.ZoneId;

@RestController
public class ReminderController {

    private final HabitReminderService habitReminderService;
    private final ZoneId zoneId;

    public ReminderController(HabitReminderService habitReminderService, ZoneId zoneId) {
        this.habitReminderService = habitReminderService;
        this.zoneId = zoneId;
    }

    @GetMapping("/reminders")
    public List<ReminderResponse> getPendingReminders() {
        return habitReminderService.getPendingReminders(LocalDate.now(zoneId)).stream()
                .map(ReminderResponse::from)
                .toList();
    }

    public record ReminderResponse(Long id, Long habitId, String habitName,
                                   LocalDate date, String reminderTime,
                                   String message, String frequency, String weekdays) {
        static ReminderResponse from(HabitReminder reminder) {
            return new ReminderResponse(
                    reminder.getId(),
                    reminder.getHabit().getId(),
                    reminder.getHabit().getName(),
                    reminder.getReminderDate(),
                    reminder.getReminderTime().toString(),
                    reminder.getMessage(),
                    reminder.getHabit().getFrequency().name(),
                    reminder.getHabit().getWeekdays());
        }
    }
}