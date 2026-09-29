package com.personalhabitstreaktracker.habitforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HabitforgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(HabitforgeApplication.class, args);
    }
}