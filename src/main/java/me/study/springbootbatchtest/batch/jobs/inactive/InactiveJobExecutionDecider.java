package me.study.springbootbatchtest.batch.jobs.inactive;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;

import java.util.Random;

@Slf4j
public class InactiveJobExecutionDecider implements JobExecutionDecider {
    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        int randomNum = new Random().nextInt();
        log.info("new Random().nextInt() is " + randomNum);
        if(randomNum > 0) { // Random 객체를 사용해 정숫값을 생성하고 양수인지 확인
            log.info("FlowExecutionStatus.COMPLETED");
            return FlowExecutionStatus.COMPLETED; // 양수
        }
        log.info("FlowExecutionStatus.FAILED");
        return FlowExecutionStatus.FAILED; // 음수
    }
}
