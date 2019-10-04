package me.study.springbootbatchtest.batch.jobs;

import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AllArgsConstructor
@Configuration
public class InactiveUserConfig {
    @Bean
    public Job inactiveUserJOb(JobBuilderFactory jobBuilderFactory, Step inactiveJobStep) {
        return jobBuilderFactory.get("inaciveUserJob")
                .preventRestart()
                .start(inactiveJobStep)
                .build();
    }
}
