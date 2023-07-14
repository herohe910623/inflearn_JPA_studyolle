package com.studyolle.modules.event.event;

import com.studyolle.infra.config.AppProperties;
import com.studyolle.infra.mail.EmailMessage;
import com.studyolle.infra.mail.EmailService;
import com.studyolle.modules.account.Account;
import com.studyolle.modules.event.Enrollment;
import com.studyolle.modules.event.Event;
import com.studyolle.modules.notification.Notification;
import com.studyolle.modules.notification.NotificationRepository;
import com.studyolle.modules.notification.NotificationType;
import com.studyolle.modules.study.Study;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;

@Slf4j
@Async
@Component
@Transactional
@RequiredArgsConstructor
public class EnrollmentEventListener {

    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;
    private final NotificationRepository notificationRepository;

    @EventListener
    public void handleEnrollmentCreatedEvent(EnrollmentEvent enrollmentEvent) {
        Enrollment enrollment = enrollmentEvent.getEnrollment();
        Account account = enrollment.getAccount();
        Event event = enrollment.getEvent();
        Study study = event.getStudy();

        if (account.isStudyEnrollmentResultByEmail()) {
            sendEmail(enrollmentEvent, account, event, study);
        }
        if (account.isStudyEnrollmentResultByWeb()) {
            createNotification(enrollmentEvent, account, study, event);
        }
    }

    private void createNotification(EnrollmentEvent enrollmentEvent, Account account, Study study, Event event) {
        //TODO Create Notification DB저장
        Notification notification = new Notification();
        notification.setTitle(study.getTitle() + "/" + event.getTitle());
        notification.setLink("/study/" + study.getEncodedPath() + "/events/" + event.getId());
        notification.setChecked(false);
        notification.setCreatedDateTime(LocalDateTime.now());
        notification.setMessage(enrollmentEvent.getMessage());
        notification.setAccount(account);
        notification.setNotificationType(NotificationType.EVENT_ENROLLMENT);
        notificationRepository.save(notification);
    }

    private void sendEmail(EnrollmentEvent enrollmentEvent, Account account, Event event, Study study) {
        //TODO 메일
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("link","/study/" + study.getEncodedPath() + "/events/" + event.getId());
        context.setVariable("linkName", study.getTitle());
        context.setVariable("message", enrollmentEvent.getMessage());
        //이메일 클릭시 링크앞에다 host 정보 appproperties 에서 가져오기
        context.setVariable("host", appProperties.getHost());

        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .subject("스터디올래, " + event.getTitle() + "모임 참가 신청 결과입니다.")
                .to(account.getEmail())
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }
}
