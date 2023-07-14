package com.studyolle.modules.event.event;

import com.studyolle.modules.event.Enrollment;
import com.studyolle.modules.event.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class EnrollmentEvent {

    private final Enrollment enrollment;
    protected final String message;

}
