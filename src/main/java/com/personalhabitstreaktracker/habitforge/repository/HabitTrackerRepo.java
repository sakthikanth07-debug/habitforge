package com.personalhabitstreaktracker.habitforge.repository;

import com.personalhabitstreaktracker.habitforge.entity.Habit;
import com.personalhabitstreaktracker.habitforge.entity.HabitFrequency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HabitTrackerRepo extends JpaRepository<Habit, Long> {

    List<Habit> findByReminderTimeIsNotNull();

    @Query("SELECT h FROM Habit h WHERE " +
           "(:name IS NULL OR LOWER(h.name) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(h.description) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:frequency IS NULL OR h.frequency = :frequency)")
    Page<Habit> searchHabits(@Param("name") String name, @Param("frequency") HabitFrequency frequency, Pageable pageable);
}