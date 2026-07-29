package com.jobportal.verification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RecruiterVerificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RecruiterVerificationServiceApplication.class, args);
    }
}
