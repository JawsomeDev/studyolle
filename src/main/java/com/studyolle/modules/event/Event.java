package com.studyolle.modules.event;


import com.studyolle.modules.account.Account;
import com.studyolle.modules.account.UserAccount;
import com.studyolle.modules.study.Study;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Entity
@Getter
@Setter @EqualsAndHashCode(of="id")
public class Event {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY)
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

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Enrollment> enrollments = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    public boolean isEnrollableFor(UserAccount userAccount) {
        return isNotClosed() && !isAttended(userAccount) && !isAlreadyEnrolled(userAccount);
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
        return enrollments.stream()
                .filter(enrollment -> !enrollment.isAccepted()) // 대기 중인 멤버만 필터링
                .sorted(Comparator.comparing(Enrollment::getEnrolledAt)) // 신청 시간 기준 정렬
                .findFirst() // 가장 먼저 신청한 멤버 반환
                .orElse(null); // 없으면 null 반환
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

    public void accept(Enrollment enrollment) {
        if(this.eventType == EventType.CONFIRMATIVE
         && this.limitOfEnrollments > this.getNumberOfAcceptedEnrollments()) {
            enrollment.setAccepted(true);
        }
    }

    public void reject(Enrollment enrollment) {
        if (this.eventType == EventType.CONFIRMATIVE) {
            enrollment.setAccepted(false);
        }
    }

    public boolean isAcceptable(Enrollment enrollment) {
        return this.eventType == EventType.CONFIRMATIVE
                && !enrollment.isAttended()
                && !enrollment.isAccepted();
    }

    public boolean isRejectable(Enrollment enrollment) {
        return this.eventType == EventType.CONFIRMATIVE
                && !enrollment.isAttended()
                && enrollment.isAccepted();
    }
}
