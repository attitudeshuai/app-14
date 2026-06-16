package com.petfoster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PetFosterApplication {
    public static void main(String[] args) {
        SpringApplication.run(PetFosterApplication.class, args);
    }
}
