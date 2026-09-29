package com.personalhabitstreaktracker.habitforge.repository;

import com.personalhabitstreaktracker.habitforge.entity.CompletionLog;
import com.personalhabitstreaktracker.habitforge.entity.Habit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

@Repository
public interface CompletionLogRepo extends JpaRepository<CompletionLog, Long> {

    List<CompletionLog> findByHabitOrderByCompletionDateAsc(Habit habit);

    List<CompletionLog> findByHabitAndCompletionDate(
            Habit habit,
            LocalDate completionDate
    );

    void deleteByHabit(Habit habit);

    List<CompletionLog> findTop10ByOrderByIdDesc();
}