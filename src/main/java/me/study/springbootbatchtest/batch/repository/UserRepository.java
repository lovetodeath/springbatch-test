package me.study.springbootbatchtest.batch.repository;

import me.study.springbootbatchtest.batch.domain.User;
import me.study.springbootbatchtest.batch.domain.enums.Grade;
import me.study.springbootbatchtest.batch.domain.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByUpdateDateBeforeAndUserStatusEquals(LocalDateTime localDateTime, UserStatus userStatus);
    List<User> findByUpdateDateBeforeAndUserStatusEqualsAndGradeEquals(LocalDateTime localDateTime, UserStatus userStatus, Grade grade);
}
