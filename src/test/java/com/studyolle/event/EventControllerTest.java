package com.studyolle.event;

import com.studyolle.WithAccount;
import com.studyolle.domain.Account;
import com.studyolle.domain.Event;
import com.studyolle.domain.EventType;
import com.studyolle.domain.Study;
import com.studyolle.study.StudyControllerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;


import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EventControllerTest extends StudyControllerTest {

    @Autowired EventService eventService;
    @Autowired EnrollmentRepository enrollmentRepository;

    @Test
    @DisplayName("선착순 모임에 참가 신청 - 자동 수락")
    @WithAccount("herohe")
    void newEnrollment_to_FCFS_event_accepted() throws Exception {
        Account account = createAccount("account");
        Study study = createStudy("test-study", account);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, account);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        Account herohe = accountRepository.findByNickname("herohe");
        assertTrue(enrollmentRepository.findByEventAndAccount(event, herohe).isAccepted());
    }

    @Test
    @DisplayName("선착순 모임에 참가 신청 - 대기중 (이미 인원이 꽉차서)")
    @WithAccount("herohe")
    void newEnrollment_to_FCFS_event_not_accepted() throws Exception {
        Account account = createAccount("account");
        Study study = createStudy("create-study", account);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, account);

        Account april = createAccount("april");
        Account may = createAccount("may");
        eventService.newEnrollment(event, april);
        eventService.newEnrollment(event, may);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        Account herohe = accountRepository.findByNickname("herohe");
        assertFalse(enrollmentRepository.findByEventAndAccount(event,herohe).isAccepted());
    }

    @Test
    @DisplayName("참가신청 확정자가 선착순 모임에 참가 신청을 취소하는 경우, 바로 다음 대기자를 자동으로 신청 확인한다.")
    @WithAccount("herohe")
    void accepted_account_cancelEnrollments_to_FCFS_event_not_accepted() throws Exception {
        Account herohe = accountRepository.findByNickname("herohe");
        Account account = createAccount("account");
        Account may = createAccount("may");
        Study study = createStudy("test-study", account);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, account);

        eventService.newEnrollment(event,herohe);
        eventService.newEnrollment(event,account);
        eventService.newEnrollment(event,may);

        //herohe 가 limit 이 2 인데 먼저 newEnrollment 호출 되었기 때문에 확정자 이다.
        assertTrue(enrollmentRepository.findByEventAndAccount(event,herohe).isAccepted());
        assertTrue(enrollmentRepository.findByEventAndAccount(event,account).isAccepted());
        assertFalse(enrollmentRepository.findByEventAndAccount(event,may).isAccepted());

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/disenroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        assertTrue(enrollmentRepository.findByEventAndAccount(event,account).isAccepted());
        assertTrue(enrollmentRepository.findByEventAndAccount(event,may).isAccepted());
        assertNull(enrollmentRepository.findByEventAndAccount(event,herohe));
    }

    @Test
    @DisplayName("참가신청 비확정자가 선착순 모임에 참가 신청을 취소하는 경우, 기존 확정자를 그대로 유지하고 새로운 확정자는 없다.")
    @WithAccount("herohe")
    void not_accepted_account_cancelEnrollment_to_FCFS_event_not_accepted() throws Exception {
        Account herohe = accountRepository.findByNickname("herohe");
        Account account = createAccount("account");
        Account may = createAccount("may");
        Study study = createStudy("test-study", account);
        Event event = createEvent("test-event", EventType.FCFS, 2, study, account);

        eventService.newEnrollment(event, account);
        eventService.newEnrollment(event, may);
        eventService.newEnrollment(event, herohe);

        // herohe가 limit 이 2 인데,3번째로 들어오기 때문에 비확정자이다.
        assertTrue(enrollmentRepository.findByEventAndAccount(event, account).isAccepted());
        assertTrue(enrollmentRepository.findByEventAndAccount(event, may).isAccepted());
        assertFalse(enrollmentRepository.findByEventAndAccount(event, herohe).isAccepted());

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/disenroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        assertTrue(enrollmentRepository.findByEventAndAccount(event, account).isAccepted());
        assertTrue(enrollmentRepository.findByEventAndAccount(event, may).isAccepted());
        assertNull(enrollmentRepository.findByEventAndAccount(event, herohe));
    }

    @Test
    @DisplayName("관리자 확인 모임에 참가 신청 - 대기중")
    @WithAccount("herohe")
    void newEnrollment_to_CONFIRMATIVE_event_not_accepted() throws Exception {
        Account account = createAccount("account");
        Study study = createStudy("test-study", account);
        Event event = createEvent("test-event", EventType.CONFIRMATIVE, 2, study, account);

        mockMvc.perform(post("/study/" + study.getPath() + "/events/" + event.getId() + "/enroll")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/study/" + study.getPath() + "/events/" + event.getId()));

        Account herohe = accountRepository.findByNickname("herohe");
        assertFalse(enrollmentRepository.findByEventAndAccount(event, herohe).isAccepted());
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
        return eventService.createEvent(event,study,account);
    }

}