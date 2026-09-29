package com.personalhabitstreaktracker.habitforge.service;

import com.personalhabitstreaktracker.habitforge.entity.Habit;
import com.personalhabitstreaktracker.habitforge.repository.HabitTrackerRepo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReminderService {

    private final HabitTrackerRepo habitTrackerRepo;

    public ReminderService(HabitTrackerRepo habitTrackerRepo) {
        this.habitTrackerRepo = habitTrackerRepo;
    }

    @Scheduled(cron = "0 * * * * *")
    public void sendReminders() {
        String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        List<Habit> habits = habitTrackerRepo.findAll();
        for (Habit habit : habits) {
            if (currentTime.equals(habit.getReminderTime()) && !habit.isCompletedToday()) {
                System.out.println("Reminder: Time to complete your habit - " + habit.getName());
            }
        }
    }
}
