package com.studyolle.modules.account;


import com.studyolle.modules.tag.Tag;
import com.studyolle.modules.study.Study;
import com.studyolle.modules.zone.Zone;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @NoArgsConstructor @AllArgsConstructor
public class Account {

    @Id @GeneratedValue
    private Long id;
    @Column(unique = true)
    private String email;
    @Column(unique = true)
    private String nickname;
    private String password;
    private boolean emailVerified;
    private String emailCheckToken;
    private LocalDateTime joinedAt;
    private LocalDateTime emailCheckTokenGeneratedAt;
    private String bio;
    private String url;
    private String occupation;
    private String location;
    @Lob @Basic(fetch = FetchType.EAGER)
    private String profileImage;
    private String company;
    private boolean studyCreatedByEmail;
    private boolean studyCreatedByWeb = true;
    private boolean studyEnrollmentResultByEmail;
    private boolean studyEnrollmentResultByWeb = true;
    private boolean studyUpdatedByEmail;
    private boolean studyUpdatedByWeb = true;
    @ManyToMany
    private Set<Tag> tags = new HashSet<>();
    @ManyToMany
    private Set<Zone> zones = new HashSet<>();

    public void generateEmailCheckToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
        this.emailCheckTokenGeneratedAt = LocalDateTime.now();
    }

    public void completeSignUp() {
        this.emailVerified = true;
        this.joinedAt = LocalDateTime.now();
    }

    public boolean isValidToken(String token) {
        return this.emailCheckToken.equals(token);
    }

    public boolean canSendConfirmEmail() {
        return this.emailCheckTokenGeneratedAt.isBefore(LocalDateTime.now().minusHours(1));
    }

    // Account 에서 Study를 참조 하려 해서 에러.. (Study 는 Study와 Event 에서만 참조받는다.)
//    public boolean isManagerOf(Study study) {
//        return study.getManagers().contains(this);
//    }

}
