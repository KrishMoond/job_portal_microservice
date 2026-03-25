package com.jobportal.resume.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        return admin;
    }

    @Bean("resumeObjectMapper")
    public ObjectMapper resumeObjectMapper() { return new ObjectMapper().findAndRegisterModules(); }

    @Bean public Queue resumeUploadedNotificationQueue() { return new Queue("resume.uploaded.notification.queue", true); }
    @Bean public Queue resumeUploadedAnalyticsQueue()    { return new Queue("resume.uploaded.analytics.queue",    true); }
}
