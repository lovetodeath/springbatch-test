package me.study.springbootbatchtest;

import me.study.springbootbatchtest.batch.domain.enums.UserStatus;
import me.study.springbootbatchtest.batch.repository.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
//@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2) // TEST에는 정의된 datasource말고 H2 DB 사용하도록
public class InactiveUserJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void 휴면_회원_전환_테스트() throws Exception {
        Date date = new Date(); // JobParameter에서는 LocalDateTime 미지원.
//        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(new JobParametersBuilder().addDate("nowDate", date).toJobParameters());

        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
        assertEquals(11, userRepository.findAll().size());
        assertEquals(0, userRepository.findByUpdateDateBeforeAndUserStatusEquals(LocalDateTime.now().minusYears(1), UserStatus.ACTIVE).size());
    }
}
