package me.study.springbootbatchtest.batch.domain;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.study.springbootbatchtest.batch.domain.enums.Grade;
import me.study.springbootbatchtest.batch.domain.enums.SocialType;
import me.study.springbootbatchtest.batch.domain.enums.UserStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table
@Getter
@EqualsAndHashCode(of = {"idx", "email"})
@NoArgsConstructor
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column
    private String name;

    @Column
    private String password;

    @Column
    private String email;

    @Column
    private String principal;

    @Column
    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    @Column
    @Enumerated(EnumType.STRING)
    private UserStatus userStatus;

    @Column
    @Enumerated(EnumType.STRING)
    private Grade grade;

    @Column
    private LocalDateTime createDate;

    @Column
    private LocalDateTime updateDate;

    public User setIncactive() {
        userStatus = UserStatus.INACTIVE;
        return this;
    }
}
