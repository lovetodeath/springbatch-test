package me.study.springbootbatchtest;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableBatchProcessing
public class SpringBootBatchTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootBatchTestApplication.class, args);
    }

}
