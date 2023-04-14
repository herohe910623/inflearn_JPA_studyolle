package com.studyolle.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Setter @Getter @EqualsAndHashCode(of = "id")
public class Event {

    @Id @GeneratedValue
    private Long id;
    @ManyToOne
    private Study study;
    @ManyToOne
    private Account createBy;
    @Column(nullable = false)
    private String title;
    @Lob
    private String description;
    @Column(nullable = false)
    private LocalDateTime createdDateTime;
    @Column(nullable = false)
    private LocalDateTime endEnrollmentDateTime;
    @Column(nullable = false)
    private LocalDateTime startDateTime;
    @Column(nullable = false)
    private LocalDateTime endDateTime;
    @Column(nullable = true)
    private Integer limitEnrollments;
    @OneToMany(mappedBy = "event")
    private List<Enrollment> enrollments;
    @Enumerated(EnumType.STRING) //기본값인 ORDINAL 이면 매핑순서가 꼬일수 있다. (배열 순서대로이기 때문)
    private EventTypes eventTypes;
}
