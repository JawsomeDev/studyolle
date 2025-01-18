package com.studyolle.domain;


import com.studyolle.account.UserAccount;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Entity
@Getter
@Setter @EqualsAndHashCode(of="id")
public class Event {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Study study;

    @ManyToOne
    private Account createdBy;

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
    private int limitOfEnrollments;

    @OneToMany(mappedBy = "event")
    private List<Enrollment> enrollments;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    public boolean isEnrollableFor(UserAccount userAccount) {
        return isNotClosed() && !isAlreadyEnrolled(userAccount);
    }

    public boolean isDisenrollableFor(UserAccount userAccount) {
        return isNotClosed() && isAlreadyEnrolled(userAccount);
    }


    private boolean isNotClosed() {
        return this.endEnrollmentDateTime.isAfter(LocalDateTime.now());
    }

    public boolean isAttended(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getAccount().equals(account)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAlreadyEnrolled(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        for (Enrollment enrollment : enrollments) {
            if(enrollment.getAccount().equals(account)) {
                return true;
            }
        }
        return false;
    }


    public int numberOfRemainSpots(){
        return this.limitOfEnrollments - (int) this.enrollments.stream().filter(Enrollment::isAccepted).count();
    }

    public Long getNumberOfAcceptedEnrollments() {
        return this.enrollments.stream().filter(Enrollment::isAccepted).count();
    }

    public boolean isFull() {
        return this.limitOfEnrollments >= this.enrollments.size();
    }
    // 연관관계 메서드: Enrollment 추가
    public void addEnrollment(Enrollment enrollment) {
        this.enrollments.add(enrollment); // Event에 Enrollment 추가
        enrollment.setEvent(this);       // Enrollment에도 Event 설정
    }

    // 연관관계 메서드: Enrollment 제거
    public void removeEnrollment(Enrollment enrollment) {
        this.enrollments.remove(enrollment); // Event에서 Enrollment 제거
        enrollment.setEvent(null);          // Enrollment에서 Event 해제
    }

    public boolean isAbleToAcceptWaitingEnrollment() {
        return this.eventType == EventType.FCFS && this.limitOfEnrollments > this.getNumberOfAcceptedEnrollments();
    }

    public Enrollment getTheFirstWaitingEnrollment() {
        for (Enrollment enrollment : enrollments) {
            if(!enrollment.isAccepted()){
                return enrollment;
            }
        }
        return null;
    }

    public void acceptNextWaitingEnrollment() {
        if(this.isAbleToAcceptWaitingEnrollment()){
            Enrollment enrollmentToAccept = this.getTheFirstWaitingEnrollment();
            if(enrollmentToAccept != null){
                enrollmentToAccept.setAccepted(true);
            }
        }
    }

    public void acceptWaitingList() {
        if(this.isAbleToAcceptWaitingEnrollment()){
            var waitingList = getWaitingList();
            int numberToAccept = (int) Math.min(this.limitOfEnrollments - this.getNumberOfAcceptedEnrollments(), waitingList.size());
            waitingList.subList(0, numberToAccept).forEach(e -> e.setAccepted(true));
        }
    }

    private List<Enrollment> getWaitingList() {
        return this.enrollments.stream().filter(Enrollment::isAccepted).collect(Collectors.toList());
    }
}
