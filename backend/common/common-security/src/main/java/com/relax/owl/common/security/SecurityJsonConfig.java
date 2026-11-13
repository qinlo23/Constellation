package com.relax.owl.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Supplies legacy response serialization without requiring another common module. */
@Configuration
public class SecurityJsonConfig {
    @Bean
    public ObjectMapper securityObjectMapper() { return new ObjectMapper(); }
}
