package com.personalhabitstreaktracker.habitforge.repository;

import com.personalhabitstreaktracker.habitforge.entity.Habit;
import com.personalhabitstreaktracker.habitforge.entity.Streak;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StreakRepo extends JpaRepository<Streak, Long> {

    Optional<Streak> findByHabit(Habit habit);
}