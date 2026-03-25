package com.jobportal.analytics.config;

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

    @Bean public Queue jobCreatedAnalyticsQueue()     { return new Queue("job.created.analytics.queue",     true); }
    @Bean public Queue jobAppliedAnalyticsQueue()     { return new Queue("job.applied.analytics.queue",     true); }
    @Bean public Queue resumeUploadedAnalyticsQueue() { return new Queue("resume.uploaded.analytics.queue", true); }
}
