package com.relax.owl.common.rabbitmq.config;

import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Generic JSON transport configuration; topology belongs to each application. */
@Configuration
public class RabbitMqConfig {
    @Bean
    public MessageConverter messageConverter() { return new JacksonJsonMessageConverter(); }
}
