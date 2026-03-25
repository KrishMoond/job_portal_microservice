package com.jobportal.notification.config;

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

    @Bean public Queue jobCreatedNotificationQueue()     { return new Queue("job.created.notification.queue",        true); }
    @Bean public Queue jobAppliedNotificationQueue()     { return new Queue("job.applied.notification.queue",        true); }
    @Bean public Queue jobClosedNotificationQueue()      { return new Queue("job.closed.notification.queue",         true); }
    @Bean public Queue resumeUploadedNotificationQueue() { return new Queue("resume.uploaded.notification.queue",    true); }
    @Bean public Queue interviewScheduledNotifQueue()    { return new Queue("interview.scheduled.notification.queue", true); }
}
