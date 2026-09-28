package com.personalhabitstreaktracker.habitforge.service;

import com.personalhabitstreaktracker.habitforge.entity.CompletionLog;
import com.personalhabitstreaktracker.habitforge.entity.Habit;
import com.personalhabitstreaktracker.habitforge.entity.HabitFrequency;
import com.personalhabitstreaktracker.habitforge.entity.Streak;
import com.personalhabitstreaktracker.habitforge.exception.HabitNotFoundException;
import com.personalhabitstreaktracker.habitforge.repository.CompletionLogRepo;
import com.personalhabitstreaktracker.habitforge.repository.HabitTrackerRepo;
import com.personalhabitstreaktracker.habitforge.repository.StreakRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class HabitService {

    private final HabitTrackerRepo habitTrackerRepo;
    private final CompletionLogRepo completionLogRepo;
    private final StreakRepo streakRepo;

    public HabitService(
            HabitTrackerRepo habitTrackerRepo,
            CompletionLogRepo completionLogRepo,
            StreakRepo streakRepo) {

        this.habitTrackerRepo = habitTrackerRepo;
        this.completionLogRepo = completionLogRepo;
        this.streakRepo = streakRepo;
    }

    @Transactional
    public Habit createHabit(Habit habit) {

        if (habit.getName() == null || habit.getName().isBlank()) {
            throw new IllegalArgumentException(
                    "Habit name is required"
            );
        }

        if (habit.getFrequency() == null) {
            throw new IllegalArgumentException(
                    "Frequency is required"
            );
        }

        if (habit.getFrequency() == HabitFrequency.SPECIFIC_WEEKDAYS) {
            if (habit.getWeekdays().isBlank()) {
                throw new IllegalArgumentException(
                        "Weekdays are required for SPECIFIC_WEEKDAYS"
                );
            }
            validateWeekdays(habit.getWeekdays());
        } else {
            habit.setWeekdays("");
        }

        normalizeReminderTime(habit);
        habit.setName(habit.getName().trim());
        habit.setCompletedToday(false);
        habit.setCurrentStreak(0);
        habit.setBestStreak(0);

        Habit savedHabit = habitTrackerRepo.saveAndFlush(habit);

        Streak streak = new Streak(savedHabit);
        streakRepo.save(streak);

        return savedHabit;
    }

    public List<Habit> getAllHabits() {
        return habitTrackerRepo.findAll();
    }

    public Habit getHabitById(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Habit id must be positive"
            );
        }

        return habitTrackerRepo.findById(id)
                .orElseThrow(() -> new HabitNotFoundException(id));
    }

    public CompletionLog completeHabit(
            Long habitId,
            LocalDate date) {

        if (habitId == null || habitId <= 0) {
            throw new IllegalArgumentException(
                    "Habit id must be positive"
            );
        }

        if (date == null) {
            throw new IllegalArgumentException(
                    "Completion date is required"
            );
        }

        Habit habit = getHabitById(habitId);

        if (!isRequiredDay(habit, date)) {
            throw new IllegalArgumentException(
                    "This date is not a required day for this habit"
            );
        }

        if (completionLogRepo
                .findByHabitAndCompletionDate(habit, date)
                .isPresent()) {

            throw new IllegalArgumentException(
                    "Habit is already completed for this date"
            );
        }

        CompletionLog log =
                new CompletionLog(habit, date);

        CompletionLog savedLog =
                completionLogRepo.save(log);

        habit.setCompletedToday(date.equals(LocalDate.now()));
        habitTrackerRepo.save(habit);
        updateStreak(habit);

        return savedLog;
    }

    public Streak getStreak(Long habitId) {

        Habit habit = getHabitById(habitId);

        return streakRepo.findByHabit(habit)
                .orElseGet(() -> {

                    Streak streak = new Streak(habit);

                    return streakRepo.save(streak);
                });
    }

    public List<CompletionLog> getCompletionLogs(Long habitId) {

        Habit habit = getHabitById(habitId);

        return completionLogRepo
                .findByHabitOrderByCompletionDateAsc(habit);
    }

    private boolean isRequiredDay(
            Habit habit,
            LocalDate date) {

        if (habit.getFrequency() == HabitFrequency.DAILY) {

            return true;
        }

        if (habit.getFrequency() == HabitFrequency.SPECIFIC_WEEKDAYS) {

            if (habit.getWeekdays() == null ||
                    habit.getWeekdays().isBlank()) {

                return false;
            }

            String day =
                    date.getDayOfWeek().toString();

            String[] weekdays =
                    habit.getWeekdays().split(",");

            for (String weekday : weekdays) {

                if (weekday.trim()
                        .equalsIgnoreCase(day)) {

                    return true;
                }
            }
        }

        return false;
    }

    private void validateWeekdays(String weekdays) {
        Set<DayOfWeek> selectedDays = new HashSet<>();
        for (String value : weekdays.split(",", -1)) {
            DayOfWeek day;
            try {
                day = DayOfWeek.valueOf(value.trim().toUpperCase());
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException("Weekdays must contain valid weekday names");
            }
            if (!selectedDays.add(day)) {
                throw new IllegalArgumentException("Weekdays must not contain duplicates");
            }
        }
    }

    private void normalizeReminderTime(Habit habit) {
        String reminderTime = habit.getReminderTime();
        if (reminderTime == null || reminderTime.isBlank()) {
            habit.setReminderTime(null);
            return;
        }

        try {
            habit.setReminderTime(LocalTime.parse(reminderTime)
                    .format(DateTimeFormatter.ofPattern("HH:mm")));
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Reminder time must use HH:mm format");
        }
    }

    private void updateStreak(Habit habit) {

        List<CompletionLog> logs =
                completionLogRepo
                        .findByHabitOrderByCompletionDateAsc(habit);

        int currentStreak =
                calculateCurrentStreak(habit, logs);

        int bestStreak =
                calculateBestStreak(habit, logs);

        Streak streak =
                streakRepo.findByHabit(habit)
                        .orElse(new Streak(habit));

        streak.setCurrentStreak(currentStreak);
        habit.setCurrentStreak(currentStreak);

        if (currentStreak > streak.getBestStreak()) {
            streak.setBestStreak(currentStreak);
        }

        if (bestStreak > streak.getBestStreak()) {
            streak.setBestStreak(bestStreak);
        }

        habit.setBestStreak(streak.getBestStreak());

        streakRepo.save(streak);
        habitTrackerRepo.save(habit);
    }

    private int calculateCurrentStreak(
            Habit habit,
            List<CompletionLog> logs) {

        if (logs.isEmpty()) {
            return 0;
        }

        LocalDate date = LocalDate.now();

        while (!isRequiredDay(habit, date)) {
            date = date.minusDays(1);
        }

        boolean completed = false;

        for (CompletionLog log : logs) {

            if (log.getCompletionDate()
                    .equals(date)) {

                completed = true;
                break;
            }
        }

        if (!completed) {
            return 0;
        }

        int streak = 0;

        while (true) {

            if (isRequiredDay(habit, date)) {

                boolean done = false;

                for (CompletionLog log : logs) {

                    if (log.getCompletionDate()
                            .equals(date)) {

                        done = true;
                        break;
                    }
                }

                if (!done) {
                    break;
                }

                streak++;
            }

            date = date.minusDays(1);

            if (date.isBefore(
                    logs.get(0).getCompletionDate())) {

                break;
            }
        }

        return streak;
    }

    private int calculateBestStreak(
            Habit habit,
            List<CompletionLog> logs) {

        if (logs.isEmpty()) {
            return 0;
        }

        int best = 0;
        int current = 0;

        LocalDate date =
                logs.get(0).getCompletionDate();

        LocalDate lastDate =
                logs.get(logs.size() - 1)
                        .getCompletionDate();

        while (!date.isAfter(lastDate)) {

            if (isRequiredDay(habit, date)) {

                boolean done = false;

                for (CompletionLog log : logs) {

                    if (log.getCompletionDate()
                            .equals(date)) {

                        done = true;
                        break;
                    }
                }

                if (done) {

                    current++;

                    if (current > best) {
                        best = current;
                    }

                } else {
                    current = 0;
                }
            }

            date = date.plusDays(1);
        }

        return best;
    }
}