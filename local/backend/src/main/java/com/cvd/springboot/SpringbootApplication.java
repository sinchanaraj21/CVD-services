package com.cvd.springboot;

import com.cvd.springboot.entity.Patient;
import com.cvd.springboot.entity.Prediction;
import com.cvd.springboot.repository.PatientRepository;
import com.cvd.springboot.repository.PredictionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

@SpringBootApplication
public class SpringbootApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootApplication.class, args);
    }


}
