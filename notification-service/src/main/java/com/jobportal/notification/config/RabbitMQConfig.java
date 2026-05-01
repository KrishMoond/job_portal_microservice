package com.jobportal.notification.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class RabbitMQConfig {

    public static final String EXCHANGE = "job.portal.exchange";

    private static final Logger log = LoggerFactory.getLogger(RabbitMQConfig.class);

    @Bean
    public TopicExchange jobPortalExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        return admin;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }

    @Bean public Queue jobCreatedNotificationQueue()       { return QueueBuilder.durable("job.created.notification.queue").build(); }
    @Bean public Queue jobAppliedNotificationQueue()       { return QueueBuilder.durable("job.applied.notification.queue").build(); }
    @Bean public Queue jobClosedNotificationQueue()        { return QueueBuilder.durable("job.closed.notification.queue").build(); }
    @Bean public Queue resumeUploadedNotificationQueue()   { return QueueBuilder.durable("resume.uploaded.notification.queue").build(); }
    @Bean public Queue interviewScheduledNotifQueue()      { return QueueBuilder.durable("interview.scheduled.notification.queue").build(); }
    @Bean public Queue appStatusChangedNotifQueue()        { return QueueBuilder.durable("application.status.changed.notification.queue").build(); }

    @Bean public Binding bindJobCreatedNotification(TopicExchange jobPortalExchange)     { return BindingBuilder.bind(jobCreatedNotificationQueue()).to(jobPortalExchange).with("job.created"); }
    @Bean public Binding bindJobAppliedNotification(TopicExchange jobPortalExchange)     { return BindingBuilder.bind(jobAppliedNotificationQueue()).to(jobPortalExchange).with("job.applied"); }
    @Bean public Binding bindJobClosedNotification(TopicExchange jobPortalExchange)      { return BindingBuilder.bind(jobClosedNotificationQueue()).to(jobPortalExchange).with("job.closed"); }
    @Bean public Binding bindResumeUploadedNotification(TopicExchange jobPortalExchange) { return BindingBuilder.bind(resumeUploadedNotificationQueue()).to(jobPortalExchange).with("resume.uploaded"); }
    @Bean public Binding bindInterviewScheduled(TopicExchange jobPortalExchange)         { return BindingBuilder.bind(interviewScheduledNotifQueue()).to(jobPortalExchange).with("interview.scheduled"); }
    @Bean public Binding bindAppStatusChanged(TopicExchange jobPortalExchange)           { return BindingBuilder.bind(appStatusChangedNotifQueue()).to(jobPortalExchange).with("application.status.changed"); }

    @Bean
    public ApplicationRunner declareQueues(RabbitAdmin rabbitAdmin) {
        return args -> {
            try {
                rabbitAdmin.initialize();
                log.info("[RabbitMQ] Exchange, queues and bindings declared successfully");
            } catch (Exception e) {
                log.error("[RabbitMQ] Failed to declare RabbitMQ resources: {}", e.getMessage(), e);
            }
        };
    }
}
