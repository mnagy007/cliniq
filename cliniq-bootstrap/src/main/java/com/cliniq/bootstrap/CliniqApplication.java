package com.cliniq.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.cliniq")
@EnableScheduling
public class CliniqApplication {

    public static void main(String[] args) {
        SpringApplication.run(CliniqApplication.class, args);
    }
}