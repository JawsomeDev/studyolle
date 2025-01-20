package com.studyolle.modules.event;

import com.studyolle.infra.AbstractContainerBaseTest;
import com.studyolle.infra.MockMvcTest;
import com.studyolle.modules.account.WithAccount;
import com.studyolle.modules.account.AccountFactory;
import com.studyolle.modules.account.AccountRepository;
import com.studyolle.modules.account.Account;
import com.studyolle.modules.study.Study;
import com.studyolle.modules.study.StudyFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@MockMvcTest
class EventControllerTest extends AbstractContainerBaseTest {

    @MockitoBean
    JavaMailSender mailSender;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    EnrollmentRepository enrollmentRepository;
    @Autowired
    private AccountFactory accountFactory;
    @Autowired
    private StudyFactory studyFactory;
    @Autowired
    private EventService eventService;


    @Test
    @DisplayName("선착순 모임에 참가 신청 - 자동 수락")
    @WithAccount("keesun")
    void newEnrollment_to_FCFS_event_accepted() throws Exception {
        Account whiteship = accountFactory.createAccount("whiteship");
        Study study = studyFactory.createStudy("test", whiteship);
        Event event = createEvent("test", EventType.FCFS, 2, study, whiteship);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        Account keesun = accountRepository.findByNickname("keesun");
        isAccepted(keesun, event);
    }

    @Test
    @DisplayName("선착순 모임에 참가 신청 - 대기중 ")
    @WithAccount("keesun")
    void newEnrollment_to_FCFS_event_rejected() throws Exception {
        Account whiteship = accountFactory.createAccount("whiteship");
        Study study = studyFactory.createStudy("test", whiteship);
        Event event = createEvent("test", EventType.FCFS, 2, study, whiteship);

        Account shark = accountFactory.createAccount("shark");
        Account choi = accountFactory.createAccount("choi");
        eventService.newEnrollment(event, shark);
        eventService.newEnrollment(event, choi);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        Account keesun = accountRepository.findByNickname("keesun");
        isNotAccepted(keesun, event);
    }

    @Test
    @DisplayName("참가 신청 확정자가 선착순 모임에 참가 신청을 취소하는 경우, 바로 다음 대기자를 자동으로 신청 확인한다.")
    @WithAccount("keesun")
    void accepted_account_cancelEnrollment_to_FCFS_event_not_accepted() throws Exception {
        Account keesun = accountRepository.findByNickname("keesun");
        Account whiteShip = accountFactory.createAccount("whiteship");
        Account shark = accountFactory.createAccount("shark");
        Study study = studyFactory.createStudy("test", whiteShip);
        Event event = createEvent("test", EventType.FCFS, 2, study, whiteShip);

        eventService.newEnrollment(event, shark);
        eventService.newEnrollment(event, keesun);
        eventService.newEnrollment(event, whiteShip);

        isAccepted(shark, event);
        isAccepted(keesun, event);
        isNotAccepted(whiteShip, event);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/disenroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        isAccepted(shark, event);
        isAccepted(whiteShip, event);
        assertNull(enrollmentRepository.findByEventAndAccount(event, keesun));

    }

    @Test
    @DisplayName("참가신청 비확정자가 선착순 모임에 참가 신청을 취소하는 경우, 기존 확정자를 그대로 유지하고 새로운 확정자는 없음.")
    @WithAccount("keesun")
    void not_accepterd_account_cancelEnrollment_to_FCFS_event_not_accepted() throws Exception {
        Account keesun = accountRepository.findByNickname("keesun");
        Account whiteship = accountFactory.createAccount("whiteship");
        Account may = accountFactory.createAccount("may");
        Study study = studyFactory.createStudy("test-study", whiteship);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, whiteship);

        eventService.newEnrollment(event, may);
        eventService.newEnrollment(event, whiteship);
        eventService.newEnrollment(event, keesun);

        isAccepted(may, event);
        isAccepted(whiteship, event);
        isNotAccepted(keesun, event);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/disenroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        isAccepted(may, event);
        isAccepted(whiteship, event);
        assertNull(enrollmentRepository.findByEventAndAccount(event, keesun));
    }

    @Test
    @DisplayName("관리자 확인 모임에 참가 신청 - 대기중")
    @WithAccount("keesun")
    void newEnrollment_to_CONFIMATIVE_event_not_accepted() throws Exception {
        Account whiteship = accountFactory.createAccount("whiteship");
        Study study = studyFactory.createStudy("test-study", whiteship);
        Event event = createEvent("test-event", EventType.CONFIRMATIVE, 2, study, whiteship);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enroll")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        Account keesun = accountRepository.findByNickname("keesun");
        isNotAccepted(keesun, event);
    }















    private void isNotAccepted(Account shark, Event event) throws Exception {
        assertFalse(enrollmentRepository.findByEventAndAccount(event, shark).isAccepted());
    }

    private void isAccepted(Account shark, Event event) throws Exception {
        assertTrue(enrollmentRepository.findByEventAndAccount(event, shark).isAccepted());
    }

    private Event createEvent(String eventTitle, EventType eventType, int limit, Study study, Account account) {
        Event event = new Event();
        event.setTitle(eventTitle);
        event.setEventType(eventType);
        event.setLimitOfEnrollments(limit);
        event.setCreatedDateTime(LocalDateTime.now());
        event.setEndEnrollmentDateTime(LocalDateTime.now().plusDays(1));
        event.setStartDateTime(LocalDateTime.now().plusDays(1).plusHours(5));
        event.setEndDateTime(LocalDateTime.now().plusDays(1).plusHours(7));
        return eventService.createEvent(event, study, account);
    }

}