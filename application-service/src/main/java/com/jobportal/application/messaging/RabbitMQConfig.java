package com.jobportal.application.messaging;

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

    @Bean public Queue jobAppliedNotificationQueue()  { return QueueBuilder.durable("job.applied.notification.queue").build(); }
    @Bean public Queue jobAppliedAnalyticsQueue()     { return QueueBuilder.durable("job.applied.analytics.queue").build(); }
    @Bean public Queue interviewScheduledNotifQueue() { return QueueBuilder.durable("interview.scheduled.notification.queue").build(); }

    @Bean public Binding bindJobAppliedNotification(TopicExchange jobPortalExchange) { return BindingBuilder.bind(jobAppliedNotificationQueue()).to(jobPortalExchange).with("job.applied"); }
    @Bean public Binding bindJobAppliedAnalytics(TopicExchange jobPortalExchange)    { return BindingBuilder.bind(jobAppliedAnalyticsQueue()).to(jobPortalExchange).with("job.applied"); }
    @Bean public Binding bindInterviewScheduled(TopicExchange jobPortalExchange)     { return BindingBuilder.bind(interviewScheduledNotifQueue()).to(jobPortalExchange).with("interview.scheduled"); }

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
