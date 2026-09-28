package com.personalhabitstreaktracker.habitforge.controller;

import com.personalhabitstreaktracker.habitforge.entity.CompletionLog;
import com.personalhabitstreaktracker.habitforge.entity.Habit;
import com.personalhabitstreaktracker.habitforge.entity.Streak;
import com.personalhabitstreaktracker.habitforge.service.HabitService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
public class HabitController {

    private final HabitService habitService;

    public HabitController(HabitService habitService) {
        this.habitService = habitService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/habits")
    public Habit createHabit(@Valid @RequestBody Habit habit) {
        return habitService.createHabit(habit);
    }

    @GetMapping("/habits")
    public List<Habit> getAllHabits() {
        return habitService.getAllHabits();
    }

    @PostMapping("/habits/get")
    public Habit getHabit(@RequestBody GetHabitRequest request) {
        return habitService.getHabitById(request.getId());
    }

    @PostMapping("/habits/complete")
    public CompletionLog completeHabit(
            @Valid @RequestBody CompletionRequest request) {

        return habitService.completeHabit(
                request.getHabitId(),
                request.getDate()
        );
    }

    @PostMapping("/habits/streak")
    public Streak getStreak(@RequestBody GetHabitRequest request) {
        return habitService.getStreak(request.getId());
    }

    @PostMapping({"/habits/calendar", "/calendar"})
    public List<CompletionLog> getCalendar(
            @Valid @RequestBody GetHabitRequest request) {
        return habitService.getCompletionLogs(request.getId());
    }

    public static class GetHabitRequest {

        @NotNull
        @Positive
        private Long id;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }

    public static class CompletionRequest {

        @NotNull
        @Positive
        private Long habitId;

        @NotNull
        private LocalDate date;

        public Long getHabitId() {
            return habitId;
        }

        public void setHabitId(Long habitId) {
            this.habitId = habitId;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }
    }
}