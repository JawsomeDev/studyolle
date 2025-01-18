package com.studyolle.event;


import com.studyolle.domain.*;
import com.studyolle.event.form.EventForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    private final EnrollmentRepository enrollmentRepository;

    public Event createEvent(Event event, Study study, Account account) {
        event.setCreatedBy(account);
        event.setCreatedDateTime(LocalDateTime.now());
        event.setStudy(study);
        return eventRepository.save(event);
    }

    public void updateEvent(Event event, @Valid EventForm eventForm) {
        modelMapper.map(eventForm, event);
        event.acceptWaitingList();
    }

    public void deleteEvent(Event event) {
        eventRepository.delete(event);
    }

    public void newEnrollment(Event event, Account account) {
        boolean alreadyEnrolled = enrollmentRepository.existsByEventAndAccount(event, account);
        if(alreadyEnrolled){
            throw new IllegalStateException("이미 신청된 사용자 입니다.");
        }
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setAccepted(event.isAbleToAcceptWaitingEnrollment());
        enrollment.setAccount(account);
        event.addEnrollment(enrollment);
        enrollmentRepository.save(enrollment);


    }

    public void cancelEnrollment(Event event, Account account) {
        Enrollment enrollment = enrollmentRepository.findByEventAndAccount(event, account);
        if (!enrollment.isAttended()) {
            event.removeEnrollment(enrollment);
            enrollmentRepository.delete(enrollment);
            event.acceptNextWaitingEnrollment();
        }
    }


}
