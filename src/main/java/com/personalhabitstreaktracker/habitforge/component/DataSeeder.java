package com.personalhabitstreaktracker.habitforge.component;

import com.personalhabitstreaktracker.habitforge.entity.CompletionLog;
import com.personalhabitstreaktracker.habitforge.entity.Habit;
import com.personalhabitstreaktracker.habitforge.entity.HabitFrequency;
import com.personalhabitstreaktracker.habitforge.repository.CompletionLogRepo;
import com.personalhabitstreaktracker.habitforge.repository.HabitTrackerRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataSeeder implements CommandLineRunner {

    private final HabitTrackerRepo habitTrackerRepo;
    private final CompletionLogRepo completionLogRepo;

    public DataSeeder(HabitTrackerRepo habitTrackerRepo, CompletionLogRepo completionLogRepo) {
        this.habitTrackerRepo = habitTrackerRepo;
        this.completionLogRepo = completionLogRepo;
    }

    @Override
    public void run(String... args) throws Exception {
        if (habitTrackerRepo.count() == 0) {
            Habit reading = new Habit();
            reading.setName("Read 10 pages");
            reading.setDescription("Read a fiction or non-fiction book.");
            reading.setFrequency(HabitFrequency.DAILY);
            reading.setReminderTime("08:00");
            reading.setCurrentStreak(5);
            reading.setBestStreak(12);
            reading = habitTrackerRepo.save(reading);

            Habit gym = new Habit();
            gym.setName("Go to the Gym");
            gym.setDescription("Weightlifting session.");
            gym.setFrequency(HabitFrequency.SPECIFIC_WEEKDAYS);
            gym.setWeekdays("MONDAY, WEDNESDAY, FRIDAY");
            gym.setReminderTime("17:00");
            gym.setCurrentStreak(2);
            gym.setBestStreak(10);
            gym = habitTrackerRepo.save(gym);

            Habit water = new Habit();
            water.setName("Drink 2L Water");
            water.setDescription("Stay hydrated.");
            water.setFrequency(HabitFrequency.DAILY);
            water.setCurrentStreak(1);
            water.setBestStreak(3);
            water = habitTrackerRepo.save(water);

            // Add history for reading
            completionLogRepo.save(new CompletionLog(reading, LocalDate.now().minusDays(1)));
            completionLogRepo.save(new CompletionLog(reading, LocalDate.now().minusDays(2)));
            completionLogRepo.save(new CompletionLog(reading, LocalDate.now().minusDays(3)));

            // Add history for gym
            completionLogRepo.save(new CompletionLog(gym, LocalDate.now().minusDays(2)));
            completionLogRepo.save(new CompletionLog(gym, LocalDate.now().minusDays(4)));

            System.out.println("Mock data seeded successfully.");
        }
    }
}
