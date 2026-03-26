package com.jobportal.job.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class RabbitMQConfig {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQConfig.class);

    @Value("${spring.rabbitmq.host:localhost}") private String host;
    @Value("${spring.rabbitmq.port:5672}")      private int port;
    @Value("${spring.rabbitmq.username:guest}") private String username;
    @Value("${spring.rabbitmq.password:guest}") private String password;

    @Bean
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory(host, port);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setConnectionNameStrategy(f -> "job-service");
        return factory;
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

    @Bean public Queue jobCreatedSearchQueue()       { return QueueBuilder.durable("job.created.search.queue").build(); }
    @Bean public Queue jobCreatedNotificationQueue() { return QueueBuilder.durable("job.created.notification.queue").build(); }
    @Bean public Queue jobCreatedAnalyticsQueue()    { return QueueBuilder.durable("job.created.analytics.queue").build(); }
    @Bean public Queue jobClosedSearchQueue()        { return QueueBuilder.durable("job.closed.search.queue").build(); }
    @Bean public Queue jobClosedNotificationQueue()  { return QueueBuilder.durable("job.closed.notification.queue").build(); }

    @Bean
    public ApplicationRunner declareQueues(RabbitAdmin rabbitAdmin) {
        return args -> {
            try {
                rabbitAdmin.initialize();
                log.info("[RabbitMQ] Queues declared successfully");
            } catch (Exception e) {
                log.error("[RabbitMQ] Failed to declare queues: {}", e.getMessage(), e);
            }
        };
    }
}
