package com.relax.owl.config;

import com.relax.owl.common.security.CurrentUserContext;
import com.relax.owl.common.log.service.OperatorProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogOperatorConfig {
    @Bean
    public OperatorProvider operatorProvider() {
        return CurrentUserContext::getUserId;
    }
}
