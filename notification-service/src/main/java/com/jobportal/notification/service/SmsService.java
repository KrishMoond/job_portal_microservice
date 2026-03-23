package com.jobportal.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    public void sendSms(String phone, String message) {
        // Integrate Twilio or AWS SNS here
        log.info("SMS to {}: {}", phone, message);
    }
}
