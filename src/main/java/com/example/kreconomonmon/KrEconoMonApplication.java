package com.example.kreconomonmon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KrEconoMonApplication {

    public static void main(String[] args) {
        SpringApplication.run(KrEconoMonApplication.class, args);
    }
}
