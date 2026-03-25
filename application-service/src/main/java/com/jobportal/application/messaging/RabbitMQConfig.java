package com.jobportal.application.messaging;

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

    @Bean("appObjectMapper")
    public ObjectMapper appObjectMapper() { return new ObjectMapper().findAndRegisterModules(); }

    @Bean public Queue jobAppliedNotificationQueue()  { return new Queue("job.applied.notification.queue",         true); }
    @Bean public Queue jobAppliedAnalyticsQueue()     { return new Queue("job.applied.analytics.queue",            true); }
    @Bean public Queue interviewScheduledNotifQueue() { return new Queue("interview.scheduled.notification.queue", true); }
}
