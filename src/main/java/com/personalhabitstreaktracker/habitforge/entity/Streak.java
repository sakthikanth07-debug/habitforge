package com.personalhabitstreaktracker.habitforge.entity;

import jakarta.persistence.*;

@Entity
public class Streak {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Habit habit;

    private int currentStreak;

    private int bestStreak;

    public Streak() {
    }

    public Streak(Habit habit) {
        this.habit = habit;
        this.currentStreak = 0;
        this.bestStreak = 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Habit getHabit() {
        return habit;
    }

    public void setHabit(Habit habit) {
        this.habit = habit;
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