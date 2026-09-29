package com.personalhabitstreaktracker.habitforge;

import com.personalhabitstreaktracker.habitforge.entity.Habit;
import com.personalhabitstreaktracker.habitforge.entity.HabitFrequency;
import com.personalhabitstreaktracker.habitforge.entity.CompletionLog;
import com.personalhabitstreaktracker.habitforge.repository.CompletionLogRepo;
import com.personalhabitstreaktracker.habitforge.repository.HabitReminderRepo;
import com.personalhabitstreaktracker.habitforge.repository.HabitTrackerRepo;
import com.personalhabitstreaktracker.habitforge.service.HabitReminderService;
import com.personalhabitstreaktracker.habitforge.service.DesktopNotificationService;
import com.personalhabitstreaktracker.habitforge.service.PushNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HabitReminderServiceTest {

    @Mock
    private HabitTrackerRepo habitTrackerRepo;

    @Mock
    private CompletionLogRepo completionLogRepo;

    @Mock
    private HabitReminderRepo habitReminderRepo;

    @Mock
    private PushNotificationService pushNotificationService;

    @Mock
    private DesktopNotificationService desktopNotificationService;

    private HabitReminderService service;

    @BeforeEach
    void setUp() {
        service = new HabitReminderService(
                habitTrackerRepo,
                completionLogRepo,
                habitReminderRepo,
                pushNotificationService,
                desktopNotificationService,
                ZoneId.of("UTC"));
    }

    @Test
    void createsReminderForIncompleteDailyHabitAfterReminderTime() {
        Habit habit = habit("Exercise", HabitFrequency.DAILY, "07:00", "");
        LocalDateTime now = LocalDateTime.of(2026, 9, 29, 7, 1);
        when(completionLogRepo.findByHabitAndCompletionDate(habit, now.toLocalDate())).thenReturn(List.of());
        when(habitReminderRepo.existsByHabitAndReminderDate(habit, now.toLocalDate())).thenReturn(false);

        service.createReminderIfDue(habit, now);

        verify(habitReminderRepo).save(any());
        verify(desktopNotificationService).sendReminderNotification(
            "Exercise", "You haven't completed Exercise today.");
    }

    @Test
    void doesNotCreateReminderBeforeReminderTime() {
        Habit habit = habit("Exercise", HabitFrequency.DAILY, "20:00", "");

        service.createReminderIfDue(habit, LocalDateTime.of(2026, 9, 29, 19, 59));

        verifyNoInteractions(completionLogRepo, habitReminderRepo);
    }

    @Test
    void doesNotCreateReminderWhenAlreadyCompleted() {
        Habit habit = habit("Exercise", HabitFrequency.DAILY, "07:00", "");
        LocalDate date = LocalDate.of(2026, 9, 29);
        when(completionLogRepo.findByHabitAndCompletionDate(habit, date)).thenReturn(List.of(mock(CompletionLog.class)));

        service.createReminderIfDue(habit, LocalDateTime.of(2026, 9, 29, 7, 1));

        verify(habitReminderRepo, never()).save(any());
    }

    @Test
    void createsReminderForScheduledSpecificWeekday() {
        Habit habit = habit("Gym", HabitFrequency.SPECIFIC_WEEKDAYS, "08:00", "MONDAY,WEDNESDAY,FRIDAY");
        LocalDateTime now = LocalDateTime.of(2026, 9, 30, 8, 1);
        when(completionLogRepo.findByHabitAndCompletionDate(habit, now.toLocalDate())).thenReturn(List.of());
        when(habitReminderRepo.existsByHabitAndReminderDate(habit, now.toLocalDate())).thenReturn(false);

        service.createReminderIfDue(habit, now);

        verify(habitReminderRepo).save(any());
    }

    @Test
    void skipsNonScheduledSpecificWeekday() {
        Habit habit = habit("Gym", HabitFrequency.SPECIFIC_WEEKDAYS, "08:00", "MONDAY,WEDNESDAY,FRIDAY");

        service.createReminderIfDue(habit, LocalDateTime.of(2026, 9, 29, 8, 1));

        verifyNoInteractions(completionLogRepo, habitReminderRepo);
    }

    @Test
    void skipsHabitWithoutReminderTime() {
        Habit habit = habit("Reading", HabitFrequency.DAILY, null, "");

        service.createReminderIfDue(habit, LocalDateTime.of(2026, 9, 29, 23, 0));

        verifyNoInteractions(completionLogRepo, habitReminderRepo);
    }

    @Test
    void doesNotDuplicateReminderForSameDate() {
        Habit habit = habit("Exercise", HabitFrequency.DAILY, "07:00", "");
        LocalDate date = LocalDate.of(2026, 9, 29);
        when(completionLogRepo.findByHabitAndCompletionDate(habit, date)).thenReturn(List.of());
        when(habitReminderRepo.existsByHabitAndReminderDate(habit, date)).thenReturn(true);

        service.createReminderIfDue(habit, LocalDateTime.of(2026, 9, 29, 7, 2));

        verify(habitReminderRepo, never()).save(any());
    }

    @Test
    void continuesProcessingWhenOneHabitFails() {
        Habit first = habit("Broken", HabitFrequency.DAILY, "00:00", "");
        Habit second = habit("Healthy", HabitFrequency.DAILY, "00:00", "");
        LocalDate today = LocalDate.now(ZoneId.of("UTC"));
        when(habitTrackerRepo.findByReminderTimeIsNotNull()).thenReturn(List.of(first, second));
        when(completionLogRepo.findByHabitAndCompletionDate(first, today))
                .thenThrow(new IllegalStateException("database error"));
        when(completionLogRepo.findByHabitAndCompletionDate(second, today))
                .thenReturn(List.of());
        when(habitReminderRepo.existsByHabitAndReminderDate(second, today))
                .thenReturn(false);

        service.checkDueReminders();

        verify(habitReminderRepo).save(any());
    }

    @Test
    void resolvesReminderForCompletedHabitDate() {
        Habit habit = habit("Exercise", HabitFrequency.DAILY, "07:00", "");
        LocalDate date = LocalDate.of(2026, 9, 29);

        service.resolveReminder(habit, date);

        verify(habitReminderRepo).deleteByHabitAndReminderDate(habit, date);
    }

    private Habit habit(String name, HabitFrequency frequency, String reminderTime, String weekdays) {
        Habit habit = new Habit(name, "", frequency, weekdays, reminderTime);
        habit.setId((long) name.hashCode());
        return habit;
    }
}