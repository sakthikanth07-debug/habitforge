package com.personalhabitstreaktracker.habitforge.repository;

import com.personalhabitstreaktracker.habitforge.entity.Habit;
import com.personalhabitstreaktracker.habitforge.entity.HabitReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface HabitReminderRepo extends JpaRepository<HabitReminder, Long> {

    @Query("SELECT r FROM HabitReminder r JOIN FETCH r.habit " +
            "WHERE r.reminderDate = :date ORDER BY r.reminderTime ASC, r.id ASC")
    List<HabitReminder> findPendingByDate(@Param("date") LocalDate date);

    boolean existsByHabitAndReminderDate(Habit habit, LocalDate reminderDate);

    long deleteByHabitAndReminderDate(Habit habit, LocalDate reminderDate);

    long deleteByHabit(Habit habit);
}