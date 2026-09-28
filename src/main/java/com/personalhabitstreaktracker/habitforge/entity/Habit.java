package com.personalhabitstreaktracker.habitforge.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
public class Habit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    private HabitFrequency frequency;

    private String weekdays;

    private String reminderTime;

    @Column(name = "completed_today", nullable = false)
    private boolean completedToday = false;

    @Column(name = "current_streak", nullable = false)
    private int currentStreak = 0;

    @Column(name = "longest_streak", nullable = false)
    private int bestStreak = 0;

    public Habit() {
    }

    public Habit(String name, String description, HabitFrequency frequency,
                 String weekdays, String reminderTime) {
        this.name = name;
        this.description = description;
        this.frequency = frequency;
        this.weekdays = weekdays;
        this.reminderTime = reminderTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public HabitFrequency getFrequency() {
        return frequency;
    }

    public void setFrequency(HabitFrequency frequency) {
        this.frequency = frequency;
    }

    public String getWeekdays() {
        return weekdays;
    }

    public void setWeekdays(String weekdays) {
        this.weekdays = weekdays == null || weekdays.isBlank()
            ? ""
            : weekdays.trim();
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime == null || reminderTime.isBlank()
                ? null
                : reminderTime.trim();
    }

    public boolean isCompletedToday() {
        return completedToday;
    }

    public void setCompletedToday(boolean completedToday) {
        this.completedToday = completedToday;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public int getBestStreak() {
        return bestStreak;
    }

    public void setBestStreak(int bestStreak) {
        this.bestStreak = bestStreak;
    }
}