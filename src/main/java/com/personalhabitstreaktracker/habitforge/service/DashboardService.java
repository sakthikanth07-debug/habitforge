package com.personalhabitstreaktracker.habitforge.service;

import com.personalhabitstreaktracker.habitforge.entity.CompletionLog;
import com.personalhabitstreaktracker.habitforge.entity.Habit;
import com.personalhabitstreaktracker.habitforge.repository.CompletionLogRepo;
import com.personalhabitstreaktracker.habitforge.repository.HabitTrackerRepo;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final HabitTrackerRepo habitTrackerRepo;
    private final CompletionLogRepo completionLogRepo;

    public DashboardService(HabitTrackerRepo habitTrackerRepo, CompletionLogRepo completionLogRepo) {
        this.habitTrackerRepo = habitTrackerRepo;
        this.completionLogRepo = completionLogRepo;
    }

    public Map<String, Object> getDashboardStats() {
        List<Habit> allHabits = habitTrackerRepo.findAll();
        long totalHabits = allHabits.size();
        long completedToday = allHabits.stream().filter(Habit::isCompletedToday).count();
        long pendingToday = totalHabits - completedToday;
        
        int bestCurrentStreak = allHabits.stream()
                .mapToInt(Habit::getCurrentStreak)
                .max()
                .orElse(0);
                
        long totalCompletions = completionLogRepo.count();
        List<CompletionLog> recentCompletions = completionLogRepo.findTop10ByOrderByIdDesc();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalHabits", totalHabits);
        stats.put("completedToday", completedToday);
        stats.put("pendingToday", pendingToday);
        stats.put("bestCurrentStreak", bestCurrentStreak);
        stats.put("totalCompletions", totalCompletions);
        
        List<Map<String, Object>> recent = recentCompletions.stream().map(log -> {
            Map<String, Object> r = new HashMap<>();
            r.put("habitId", log.getHabit().getId());
            r.put("name", log.getHabit().getName());
            r.put("date", log.getCompletionDate());
            return r;
        }).collect(Collectors.toList());
        stats.put("recentCompletions", recent);

        return stats;
    }
}
