package me.study.springbootbatchtest.batch.jobs;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.study.springbootbatchtest.batch.domain.User;
import me.study.springbootbatchtest.batch.domain.enums.Grade;
import me.study.springbootbatchtest.batch.domain.enums.UserStatus;
import me.study.springbootbatchtest.batch.jobs.inactive.InactiveJobExecutionDecider;
import me.study.springbootbatchtest.batch.jobs.inactive.InactiveStepListenerAnnotation;
import me.study.springbootbatchtest.batch.jobs.inactive.InactiveUserPartitioner;
import me.study.springbootbatchtest.batch.jobs.readers.QueueItemReader;
import me.study.springbootbatchtest.batch.repository.UserRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@AllArgsConstructor
@Configuration
@Slf4j
public class InactiveUserJobConfig {
    private UserRepository userRepository;
    private final static int CHUNK_SIZE = 5;
    private final EntityManagerFactory entityManagerFactory; // 트랜잭션 처리

//    @Bean
//    public Job inactiveUserJob(JobBuilderFactory jobBuilderFactory, Step inactiveJobStep) {
//        log.info("InactiveUserJobConfig.inactiveUserJob log");
//        return jobBuilderFactory.get("inactiveJob")
//                .preventRestart()
//                .start(inactiveJobStep)
//                .build();
//    }

//    @Bean
//    public Job inactiveUserJobParam(JobBuilderFactory jobBuilderFactory, Step inactiveJobStepParam) {
//        log.info("InactiveUserJobConfig.inactiveUserJob log");
//        return jobBuilderFactory.get("inactiveJob")
//                .preventRestart()
//                .start(inactiveJobStepParam)
//                .build();
//    }

//    @Bean
//    public Job inactiveUserJobParamListener(JobBuilderFactory jobBuilderFactory, Step inactiveJobStepParam, JobExecutionListener inactiveJobListener) {
//        log.info("InactiveUserJobConfig.inactiveUserJob log");
//        return jobBuilderFactory.get("inactiveJob")
//                .preventRestart()
//                .listener(inactiveJobListener)
//                .start(inactiveJobStepParam)
//                .build();
//    }

//    @Bean
//    public Job inactiveUserJobParamListener(JobBuilderFactory jobBuilderFactory, Step inactiveJobStepParamListener, JobExecutionListener inactiveJobListener) {
//        log.info("InactiveUserJobConfig.inactiveUserJob log");
//        return jobBuilderFactory.get("inactiveJob")
//                .preventRestart()
//                .listener(inactiveJobListener)
//                .start(inactiveJobStepParamListener)
//                .build();
//    }

//    @Bean
//    public Job inactiveUserJobParamListenerTask(JobBuilderFactory jobBuilderFactory, Step inactiveJobStepParamListenerTask, JobExecutionListener inactiveJobListener) {
//        log.info("InactiveUserJobConfig.inactiveUserJob log");
//        return jobBuilderFactory.get("inactiveJob")
//                .preventRestart()
//                .listener(inactiveJobListener)
//                .start(inactiveJobStepParamListenerTask)
//                .build();
//    }

//    @Bean
//    public Job inactiveUserJobParamListenerFlow(JobBuilderFactory jobBuilderFactory, Flow inactiveJobFlow, JobExecutionListener inactiveJobListener) {
//        log.info("InactiveUserJobConfig.inactiveUserJob log");
//        return jobBuilderFactory.get("inactiveJob")
//                .preventRestart()
//                .listener(inactiveJobListener)
//                // start에 Step 말고 Flow 지정 (Flow에서 Step 호출)
//                .start(inactiveJobFlow)
//                .end()
//                .build();
//    }

//    @Bean
//    public Job inactiveUserJobParamListenerMultiFlow(JobBuilderFactory jobBuilderFactory, Flow inactiveJobMultiFlow, JobExecutionListener inactiveJobListener) {
//        log.info("InactiveUserJobConfig.inactiveUserJob log");
//        return jobBuilderFactory.get("inactiveJob")
//                .preventRestart()
//                .listener(inactiveJobListener)
//                // start에 Step 말고 Flow 지정 (Flow에서 Step 호출)
//                .start(inactiveJobMultiFlow)
//                .end()
//                .build();
//    }

    @Bean
    public Job inactiveUserJobParamListenerPartition(JobBuilderFactory jobBuilderFactory, JobExecutionListener inactiveJobListener, Step partitionerStep) {
        log.info("InactiveUserJobConfig.inactiveUserJobParamListenerPartition log");
        return jobBuilderFactory.get("inactiveJob")
                .preventRestart()
                .listener(inactiveJobListener)
                .start(partitionerStep)
                .build();
    }

    @Bean
    @JobScope // Job 실행 때마다 빈을 새로 생성
    public Step partitionerStep(StepBuilderFactory stepBuilderFactory, Step inactiveJobStepParamListenerPartition) {
        log.info("InactiveUserJobConfig.partitionerStep log");
        return stepBuilderFactory
                .get("partitionerStep")
                // partitioner 설정
                .partitioner("partitionerStep", new InactiveUserPartitioner())
                .gridSize(5)
                .step(inactiveJobStepParamListenerPartition)
                .taskExecutor(inactiveTaskExecutor())
                .build();
    }

    @Bean
    public Flow inactiveJobMultiFlow(Step inactiveJobStepParamListener) {
        Flow[] flows = new Flow[5];
        // 배열 크기만큼 loop 돌면서 Flow 생성
        IntStream.range(0, flows.length).forEach(i -> flows[i] =
                new FlowBuilder<Flow>("MultiFlow"+1).from(inactiveJobFlow(inactiveJobStepParamListener)).end());
        FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("inactiveJobMultiFlow");
        return flowBuilder
                // taskExecutor 설정
                .split(inactiveTaskExecutor())
                // 배열 크기만큼 생성된 Flow 배열 추가
                .add(flows)
                .build();
    }

    @Bean
    public Flow inactiveJobFlow(Step inactiveJobStepParamListener) {
        FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("inactiveJobFlow");
        return flowBuilder
                // 조건식 먼저 수행
                .start(new InactiveJobExecutionDecider())
                // JobExecutionDecider 클래스의 decide() 메소드 반환값이 FlowExecutionStatus.FAILED이면 end() 호출해서 배치미수행!
                .on(FlowExecutionStatus.FAILED.getName()).end()
                // JobExecutionDecider 클래스의 decide() 메소드 반환값이 FlowExecutionStatus.COMPLETED이면 STEP 호출해서 배치수행!
                .on(FlowExecutionStatus.COMPLETED.getName()).to(inactiveJobStepParamListener)
                .end();
    }

    @Bean
    public Step inactiveJobStep(StepBuilderFactory stepBuilderFactory) {
        log.info("InactiveUserJobConfig.inactiveJobStep log");
        return stepBuilderFactory.get("inactiveUserStep")
                .<User, User>  chunk(10)
//                .reader(inactiveUserReaderQueue())
//                .reader(inactiveUserReaderList())
//                .reader(inactiveUserReaderJpa())
                .reader(inactiveUserReaderList())
                .processor(inactiveUserProcessor())
//                .writer(inactiveUserWriter())
                .writer(inactiveUserWriterJpa())
                .build();
    }

    @Bean
    public Step inactiveJobStepParam(StepBuilderFactory stepBuilderFactory, @Qualifier("inactiveUserReaderListParam") ListItemReader<User> listItemReader) {
        log.info("InactiveUserJobConfig.inactiveJobStepParam log");
        return stepBuilderFactory.get("inactiveJobStepParam")
                .<User, User>  chunk(10)
//                .reader(inactiveUserReaderQueue())
//                .reader(inactiveUserReaderList())
//                .reader(inactiveUserReaderJpa())
                .reader(listItemReader)
                .processor(inactiveUserProcessor())
//                .writer(inactiveUserWriter())
                .writer(inactiveUserWriterJpa())
                .build();
    }

    @Bean
    public Step inactiveJobStepParamListener(StepBuilderFactory stepBuilderFactory, @Qualifier("inactiveUserReaderListParam") ListItemReader<User> listItemReader, InactiveStepListenerAnnotation inactiveStepListenerAnnotation) {
        log.info("InactiveUserJobConfig.inactiveJobStepParam log");
        return stepBuilderFactory.get("inactiveJobStepParam")
                .<User, User>  chunk(10)
                .reader(listItemReader)
                .processor(inactiveUserProcessor())
                .writer(inactiveUserWriterJpa())
                .listener(inactiveStepListenerAnnotation)
                .build();
    }

    @Bean
    public Step inactiveJobStepParamListenerPartition(StepBuilderFactory stepBuilderFactory, @Qualifier("inactiveUserReaderListParamPartition") ListItemReader<User> listItemReader, InactiveStepListenerAnnotation inactiveStepListenerAnnotation) {
        log.info("InactiveUserJobConfig.inactiveJobStepParam log");
        return stepBuilderFactory.get("inactiveJobStepParam")
                .<User, User>  chunk(10)
                .reader(listItemReader)
                .processor(inactiveUserProcessor())
                .writer(inactiveUserWriterJpa())
                .listener(inactiveStepListenerAnnotation)
                .build();
    }

    @Bean
    public Step inactiveJobStepParamListenerTask(StepBuilderFactory stepBuilderFactory, @Qualifier("inactiveUserReaderListParam") ListItemReader<User> listItemReader
            , InactiveStepListenerAnnotation inactiveStepListenerAnnotation, TaskExecutor inactiveTaskExecutor) {
        log.info("InactiveUserJobConfig.inactiveJobStepParamListenerTask log");
        return stepBuilderFactory.get("inactiveJobStepParamListenerTask")
                .<User, User>  chunk(CHUNK_SIZE)
                .reader(listItemReader)
                .processor(inactiveUserProcessor())
                .writer(inactiveUserWriterJpa())
                .listener(inactiveStepListenerAnnotation)
                .taskExecutor(inactiveTaskExecutor)
                // 설정한 제한 횟수만큼만 스레드를 동시에 실행, 1은 기존과 같음. 2부터 스레드 실행
                // 단, 시스템에 할당된 스레드 풀의 크기보다 작은 값으로 설정되어야 함.
                .throttleLimit(2)
                .build();
    }

    @Bean
    // TaskExecutionAutoConfiguration.class에 taskExecutor bean이 있어서 메소드 이름을 taskExecutor로 설정하면 호출 시 오류
    public TaskExecutor inactiveTaskExecutor() {
        // threadNamePrifix 이후 순번이 붙음. Batch_Task1 ...
        return new SimpleAsyncTaskExecutor("Batch_Task");
    }

    @Bean
    @StepScope // 각 Step의 실행마다 새로 빈을 만들기 때문에 지연 생성이 가능
    public QueueItemReader<User> inactiveUserReaderQueue() {
        List<User> oldUsers = userRepository.findByUpdateDateBeforeAndUserStatusEquals(LocalDateTime.now().minusYears(1), UserStatus.ACTIVE);
        log.info("oldUsers.size() ->" + oldUsers.size());
        return new QueueItemReader<>(oldUsers);
    }

    @Bean(name = "inactiveUserReaderList")
    @StepScope // 각 Step의 실행마다 새로 빈을 만들기 때문에 지연 생성이 가능
    public ListItemReader<User> inactiveUserReaderList() {
        List<User> oldUsers = userRepository.findByUpdateDateBeforeAndUserStatusEquals(LocalDateTime.now().minusYears(1), UserStatus.ACTIVE);
        log.info("oldUsers.size() ->" + oldUsers.size());
        return new ListItemReader<>(oldUsers);
    }

    @Bean(name = "inactiveUserReaderListParam")
    @StepScope // 각 Step의 실행마다 새로 빈을 만들기 때문에 지연 생성이 가능
    // 예제에선 UserRepository를 매개변수로 추가했지만, 없어도 오류가 발생하지는 않음.
//    public ListItemReader<User> inactiveUserReaderListParam(@Value("#{jobParameters[nowDate]}") Date nowDate, UserRepository userRepository) {
    public ListItemReader<User> inactiveUserReaderListParam(@Value("#{jobParameters[nowDate]}") Date nowDate) {
        log.info("nowDate = " + nowDate);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(nowDate.toInstant(), ZoneId.systemDefault());
        List<User> oldUsers = userRepository.findByUpdateDateBeforeAndUserStatusEquals(localDateTime.now().minusYears(1), UserStatus.ACTIVE);
        log.info("oldUsers.size() ->" + oldUsers.size());
        return new ListItemReader<>(oldUsers);
    }

    @Bean(name = "inactiveUserReaderListParamPartition")
    @StepScope // 각 Step의 실행마다 새로 빈을 만들기 때문에 지연 생성이 가능
    // 예제에선 UserRepository를 매개변수로 추가했지만, 없어도 오류가 발생하지는 않음.
    // SpEL을 사용하여 ExecutionContext에 할당한 등급값 조회
    public ListItemReader<User> inactiveUserReaderListParamPartition(@Value("#{stepExecutionContext[grade]}") String grade, UserRepository userRepository) {
        log.info("grade = " + grade);
        List<User> inactiveUsers = userRepository.findByUpdateDateBeforeAndUserStatusEqualsAndGradeEquals(LocalDateTime.now().minusYears(1), UserStatus.ACTIVE, Grade.valueOf(grade));
        log.info("inactiveUsers.size() ->" + inactiveUsers.size());
        return new ListItemReader<>(inactiveUsers);
    }

    @Bean(destroyMethod = "") // destory method 미사용
    @StepScope // 각 Step의 실행마다 새로 빈을 만들기 때문에 지연 생성이 가능
    public JpaPagingItemReader<User> inactiveUserReaderJpa() {
        JpaPagingItemReader<User> jpaPagingItemReader = new JpaPagingItemReader<>();
//        JpaPagingItemReader<User> jpaPagingItemReader = new JpaPagingItemReader<>() {
//            @Override
//            public int getPage() {
//                return 0;
//            }
//        };
        log.info("jpaPagingItemReader in");
        // jpaPagingItemReader에는 쿼리문을 직접 작성하는 방법만 지원
        jpaPagingItemReader.setQueryString("select u from User as u where u.updateDate < :updateDate and u.userStatus = :userStatus");
        Map<String, Object> map = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        map.put("updateDate", now.minusYears(1));
        map.put("userStatus", UserStatus.ACTIVE);

        jpaPagingItemReader.setParameterValues(map);
        jpaPagingItemReader.setEntityManagerFactory(entityManagerFactory); // 트랜잭션 관리
        jpaPagingItemReader.setPageSize(CHUNK_SIZE);
        return jpaPagingItemReader;
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

    // 별다른 설정 필요 없이 Generic에 저장할 타입 명시 후 entityManagerFactory만 설정하면
    // Processor 에서 넘어온 데이터를 청크 단위로 저정한다.
    private JpaItemWriter<User> inactiveUserWriterJpa() {
        JpaItemWriter<User> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }
}