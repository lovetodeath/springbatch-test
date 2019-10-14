package me.study.springbootbatchtest.batch.jobs.inactive;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InactiveStepListenerAnnotation {
    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        log.info("InactiveStepListenerAnnotation beforeStep");
    }

    @AfterStep
    public void afterStep(StepExecution stepExecution) {
        log.info("InactiveStepListenerAnnotation afterStep");
    }
}
