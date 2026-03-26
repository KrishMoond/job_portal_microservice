package com.jobportal.search.config;

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

    @Bean public Queue jobCreatedSearchQueue() { return QueueBuilder.durable("job.created.search.queue").build(); }
    @Bean public Queue jobClosedSearchQueue()  { return QueueBuilder.durable("job.closed.search.queue").build(); }

    @Bean public Binding bindJobCreatedSearch(TopicExchange jobPortalExchange) { return BindingBuilder.bind(jobCreatedSearchQueue()).to(jobPortalExchange).with("job.created"); }
    @Bean public Binding bindJobClosedSearch(TopicExchange jobPortalExchange)  { return BindingBuilder.bind(jobClosedSearchQueue()).to(jobPortalExchange).with("job.closed"); }

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
