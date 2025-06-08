package com.example.clb.projecttracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching 
@EnableAsync
@EnableMongoAuditing
@EnableScheduling 
public class ProjectTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectTrackerApplication.class, args);
    }

}
