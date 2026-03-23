package com.jobportal.job.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageType;

@Configuration
@EnableJms
public class ActiveMQConfig {

    @Value("${spring.activemq.broker-url}") private String brokerUrl;
    @Value("${spring.activemq.user}") private String user;
    @Value("${spring.activemq.password}") private String password;

    @Bean
    public ActiveMQConnectionFactory connectionFactory() {
        ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(user, password, brokerUrl);
        cf.setTrustAllPackages(true);
        return cf;
    }

    @Bean
    public MappingJackson2MessageConverter messageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        // _type header is used by the consumer to deserialize to the correct class
        converter.setTypeIdPropertyName("_type");
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        converter.setObjectMapper(mapper);
        return converter;
    }

    @Bean
    public JmsTemplate jmsTemplate() {
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory());
        jmsTemplate.setMessageConverter(messageConverter());
        // false = point-to-point queues (reliable, no lost messages on consumer restart)
        jmsTemplate.setPubSubDomain(false);
        jmsTemplate.setDeliveryPersistent(true);
        return jmsTemplate;
    }
}
