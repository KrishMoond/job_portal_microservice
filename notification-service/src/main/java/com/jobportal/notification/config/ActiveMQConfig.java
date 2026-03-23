package com.jobportal.notification.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.util.ErrorHandler;

@Configuration
@EnableJms
public class ActiveMQConfig {

    private static final Logger log = LoggerFactory.getLogger(ActiveMQConfig.class);

    @Value("${spring.activemq.broker-url}") private String brokerUrl;
    @Value("${spring.activemq.user}") private String user;
    @Value("${spring.activemq.password}") private String password;

    @Bean
    public ActiveMQConnectionFactory notifConnectionFactory() {
        ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(user, password, brokerUrl);
        cf.setTrustAllPackages(true);
        // Retry up to 3 times with 2s backoff before sending to DLQ (ActiveMQ.DLQ)
        RedeliveryPolicy policy = new RedeliveryPolicy();
        policy.setMaximumRedeliveries(3);
        policy.setInitialRedeliveryDelay(2000L);
        policy.setBackOffMultiplier(2.0);
        policy.setUseExponentialBackOff(true);
        cf.setRedeliveryPolicy(policy);
        return cf;
    }

    @Bean
    public MappingJackson2MessageConverter notifMessageConverter() {
        MappingJackson2MessageConverter c = new MappingJackson2MessageConverter();
        c.setTargetType(MessageType.TEXT);
        c.setTypeIdPropertyName("_type");
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        c.setObjectMapper(mapper);
        return c;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(notifConnectionFactory());
        factory.setMessageConverter(notifMessageConverter());
        // false = queues, must match producer's setPubSubDomain(false)
        factory.setPubSubDomain(false);
        factory.setConcurrency("1-5");
        // Log errors without crashing the listener thread
        factory.setErrorHandler(t ->
            log.error("[JMS-CONSUMER] Listener error: {}", t.getMessage(), t));
        return factory;
    }
}
