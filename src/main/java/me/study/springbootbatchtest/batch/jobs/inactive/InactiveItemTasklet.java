package me.study.springbootbatchtest.batch.jobs.inactive;

import lombok.AllArgsConstructor;
import me.study.springbootbatchtest.batch.domain.User;
import me.study.springbootbatchtest.batch.domain.enums.UserStatus;
import me.study.springbootbatchtest.batch.repository.UserRepository;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class InactiveItemTasklet implements Tasklet {
    private UserRepository userRepository;

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        //reader
        Date date = (Date) chunkContext.getStepContext().getJobParameters().get("nowDate");
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        List<User> inactiveUserList = userRepository.findByUpdateDateBeforeAndUserStatusEquals(localDateTime.minusYears(1), UserStatus.ACTIVE);

        //process
        inactiveUserList = inactiveUserList.stream().map(User::setIncactive).collect(Collectors.toList());

        //writer
        userRepository.saveAll(inactiveUserList);

        return RepeatStatus.FINISHED;
    }
}
