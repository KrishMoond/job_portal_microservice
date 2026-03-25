package com.jobportal.job.messaging;

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

    @Bean("jobObjectMapper")
    public ObjectMapper jobObjectMapper() { return new ObjectMapper().findAndRegisterModules(); }

    @Bean public Queue jobCreatedSearchQueue()       { return new Queue("job.created.search.queue",        true); }
    @Bean public Queue jobCreatedNotificationQueue() { return new Queue("job.created.notification.queue",  true); }
    @Bean public Queue jobCreatedAnalyticsQueue()    { return new Queue("job.created.analytics.queue",     true); }
    @Bean public Queue jobClosedSearchQueue()        { return new Queue("job.closed.search.queue",         true); }
    @Bean public Queue jobClosedNotificationQueue()  { return new Queue("job.closed.notification.queue",   true); }
}
