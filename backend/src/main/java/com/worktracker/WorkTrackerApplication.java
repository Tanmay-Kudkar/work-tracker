package com.worktracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WorkTrackerApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorkTrackerApplication.class, args);
    }
}
