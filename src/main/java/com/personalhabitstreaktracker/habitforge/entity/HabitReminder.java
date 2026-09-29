package com.personalhabitstreaktracker.habitforge.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "habit_reminder", uniqueConstraints = @jakarta.persistence.UniqueConstraint(
    name = "uk_habit_reminder_date", columnNames = {"habit_id", "reminder_date"}))
public class HabitReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "habit_id", nullable = false)
    private Habit habit;

    @Column(name = "reminder_date", nullable = false)
    private LocalDate reminderDate;

    @Column(name = "reminder_time", nullable = false)
    private LocalTime reminderTime;

    @Column(nullable = false, length = 255)
    private String message;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    protected HabitReminder() {
    }

    public HabitReminder(Habit habit, LocalDate reminderDate, LocalTime reminderTime,
                         String message, LocalDateTime sentAt) {
        this.habit = habit;
        this.reminderDate = reminderDate;
        this.reminderTime = reminderTime;
        this.message = message;
        this.sentAt = sentAt;
    }

    public Long getId() {
        return id;
    }

    public Habit getHabit() {
        return habit;
    }

    public LocalDate getReminderDate() {
        return reminderDate;
    }

    public LocalTime getReminderTime() {
        return reminderTime;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }
}