package me.study.springbootbatchtest.test;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

public class JobTest {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    public JobTest() {

    }

    @Bean
    public Job simpleJob() {
        return jobBuilderFactory.get("simpleJob")
                .start(simpleStep())
                .build();
    }

    private Step simpleStep() {
        return null;
    }
}
