package com.relax.owl.common.log.config;

import com.relax.owl.common.log.aspect.OperationLogAspect;
import com.relax.owl.common.log.filter.TraceIdFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/** Standalone logging configuration; no security or middleware beans are required. */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Import({OperationLogAspect.class, TraceIdFilter.class})
public class LogAutoConfiguration {
}
