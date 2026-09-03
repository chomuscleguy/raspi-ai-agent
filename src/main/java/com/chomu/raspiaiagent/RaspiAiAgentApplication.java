package com.chomu.raspiaiagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RaspiAiAgentApplication {
    public static void main(String[] args) {
        SpringApplication.run(RaspiAiAgentApplication.class, args);
    }
}