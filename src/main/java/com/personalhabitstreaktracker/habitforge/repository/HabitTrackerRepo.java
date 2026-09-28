package com.personalhabitstreaktracker.habitforge.repository;

import com.personalhabitstreaktracker.habitforge.entity.Habit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HabitTrackerRepo extends JpaRepository<Habit, Long> {
}