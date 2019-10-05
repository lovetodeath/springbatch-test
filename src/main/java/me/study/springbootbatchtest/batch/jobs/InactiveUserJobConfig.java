package me.study.springbootbatchtest.batch.jobs;

import lombok.AllArgsConstructor;
import me.study.springbootbatchtest.batch.domain.User;
import me.study.springbootbatchtest.batch.domain.enums.UserStatus;
import me.study.springbootbatchtest.batch.jobs.readers.QueueItemReader;
import me.study.springbootbatchtest.batch.repository.UserRepository;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

@AllArgsConstructor
@Configuration
public class InactiveUserJobConfig {
    private UserRepository userRepository;

    @Bean
    public Step inactiveJobStep(StepBuilderFactory stepBuilderFactory) {
        return stepBuilderFactory.get("inactiveUserStep")
                .<User, User>  chunk(10)
                .reader(inactiveUserReader())
                .processor(inactiveUserProcessor())
                .writer(inactiveUserWriter())
                .build();
    }

    @Bean
    @StepScope // 각 Step의 실행마다 새로 빈을 만들기 때문에 지연 생성이 가능
    public QueueItemReader<User> inactiveUserReader() {
        List<User> oldUsers = userRepository.findByUpdatedDateBeforeAndStatusEquals(LocalDateTime.now().minusYears(1), UserStatus.ACTIVE);
        return new QueueItemReader<>(oldUsers);
    }

    private ItemProcessor<User, User> inactiveUserProcessor() {
        return User::setIncactive;

//        return new ItemProcessor<User, User>() {
//            @Override
//            public User process(User user) throws Exception {
//                return user.setIncactive();
//            }
//        }
    }

    private ItemWriter<User> inactiveUserWriter() {
        return ((List<? extends User> users) -> userRepository.saveAll(users));
    }
}