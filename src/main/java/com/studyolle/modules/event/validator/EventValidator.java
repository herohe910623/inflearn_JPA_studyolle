package com.studyolle.modules.event.validator;

import com.studyolle.modules.event.Event;
import com.studyolle.modules.event.form.EventForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;

@Component
public class EventValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return EventForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        EventForm eventForm = (EventForm) target;

        if (isNotValidEndEnrollmentDateTime(eventForm)) {
            errors.rejectValue("endEnrollmentDateTime", "wrong.dateTime", "모임 접수 종료 일시를 정확히 입력하세요.");
        }
        if (isNotValidEndDateTime(eventForm)) {
            errors.rejectValue("endDateTime","wrong.dateTime", "모임 종료 일시를 정확히 입력하세요.");
        }
        if (isNotValidStartDateTime(eventForm)) {
            errors.rejectValue("startDateTime","wrong.dateTime", "모임 시작 일시를 정확히 입력하세요.");
        }
    }

    private boolean isNotValidEndEnrollmentDateTime(EventForm eventForm) {
        return eventForm.getEndEnrollmentDateTime().isBefore(LocalDateTime.now());
    }
    private boolean isNotValidEndDateTime(EventForm eventForm) {
        LocalDateTime endDateTime = eventForm.getEndDateTime();
        return endDateTime.isBefore(eventForm.getStartDateTime()) || endDateTime.isBefore(eventForm.getEndEnrollmentDateTime());
    }
    private boolean isNotValidStartDateTime(EventForm eventForm) {
        return eventForm.getStartDateTime().isBefore(eventForm.getEndEnrollmentDateTime());
    }

    public void validateUpdateForm(EventForm eventForm, Event event, Errors errors) {
        if (eventForm.getLimitOfEnrollments() < event.getNumberOfAcceptedEnrollments()) {
            errors.rejectValue("limitOfEnrollments","wrong.value","확인된 참가 신청보다 모집 인원 수가 커야 합니다.");
        }
    }
}
