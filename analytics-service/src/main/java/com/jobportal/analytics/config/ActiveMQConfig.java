package com.jobportal.analytics.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageType;

@Configuration
@EnableJms
public class ActiveMQConfig {
    @Value("${spring.activemq.broker-url}") private String brokerUrl;
    @Value("${spring.activemq.user}") private String user;
    @Value("${spring.activemq.password}") private String password;

    @Bean
    public ActiveMQConnectionFactory analyticsConnectionFactory() {
        ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory(user, password, brokerUrl);
        cf.setTrustAllPackages(true);
        return cf;
    }

    @Bean
    public MappingJackson2MessageConverter analyticsMessageConverter() {
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
        factory.setConnectionFactory(analyticsConnectionFactory());
        factory.setMessageConverter(analyticsMessageConverter());
        factory.setConcurrency("1-3");
        return factory;
    }
}
