package com.jobportal.resume.messaging;

import com.jobportal.common.events.ResumeUploadedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class ResumeEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ResumeEventPublisher.class);
    public static final String RESUME_UPLOADED_QUEUE = "resume.uploaded.queue";

    private final JmsTemplate resumeJmsTemplate;

    public ResumeEventPublisher(JmsTemplate resumeJmsTemplate) {
        this.resumeJmsTemplate = resumeJmsTemplate;
    }

    public void publishResumeUploaded(ResumeUploadedEvent event) {
        log.info("Publishing ResumeUploadedEvent for resumeId: {}", event.getResumeId());
        resumeJmsTemplate.convertAndSend(RESUME_UPLOADED_QUEUE, event);
    }
}
