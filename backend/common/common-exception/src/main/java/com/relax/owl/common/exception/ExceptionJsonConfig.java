package com.relax.owl.common.exception;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Supplies legacy response serialization without requiring another common module. */
@Configuration
public class ExceptionJsonConfig {
    @Bean
    public ObjectMapper exceptionObjectMapper() { return new ObjectMapper(); }
}
