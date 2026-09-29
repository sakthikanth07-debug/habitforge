package com.personalhabitstreaktracker.habitforge.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.ZoneId;

@Configuration
@EnableScheduling
public class SchedulingConfig {

    @Bean
    ZoneId habitZoneId(@Value("${habit.reminder.timezone:UTC}") String timezone) {
        return ZoneId.of(timezone);
    }
}