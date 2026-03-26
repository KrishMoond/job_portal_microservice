package com.jobportal.application.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectMapperConfig {

    @Bean("appObjectMapper")
    public ObjectMapper appObjectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }
}
