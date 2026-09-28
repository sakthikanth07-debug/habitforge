package com.personalhabitstreaktracker.habitforge.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class CompletionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Habit habit;

    private LocalDate completionDate;

    public CompletionLog() {
    }

    public CompletionLog(Habit habit, LocalDate completionDate) {
        this.habit = habit;
        this.completionDate = completionDate;
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

    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDate completionDate) {
        this.completionDate = completionDate;
    }
}