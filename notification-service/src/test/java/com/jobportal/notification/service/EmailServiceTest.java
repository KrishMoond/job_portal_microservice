package com.jobportal.notification.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock JavaMailSender mailSender;
    @InjectMocks EmailService emailService;

    @Test
    void sendEmail_success() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        emailService.sendEmail("to@example.com", "Subject", "Body");
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEmail_mailException_doesNotThrow() {
        doThrow(new MailSendException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));
        emailService.sendEmail("to@example.com", "Subject", "Body");
        verify(mailSender).send(any(SimpleMailMessage.class));
    }
}
