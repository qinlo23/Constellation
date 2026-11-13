package com.relax.owl.common.log;

import com.relax.owl.common.log.annotation.OperationLog;
import com.relax.owl.common.log.aspect.OperationLogAspect;
import com.relax.owl.common.log.config.LogAutoConfiguration;
import com.relax.owl.common.log.filter.TraceIdFilter;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.slf4j.MDC;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.slf4j.LoggerFactory;
import static org.junit.jupiter.api.Assertions.*;

class OperationLogTest {
    public static class Operations {
        final IllegalStateException failure = new IllegalStateException("original");
        @OperationLog(module = "test", action = "success")
        public String success() { return "result"; }
        @OperationLog(module = "test", action = "failure")
        public void fail() { throw failure; }
    }
    @Test void worksWithoutSecurityMqOrDatabaseAndPreservesOutcome() {
        try (var context = new AnnotationConfigApplicationContext()) {
            var target = new Operations();
            context.registerBean(Operations.class, () -> target);
            context.register(LogAutoConfiguration.class);
            context.refresh();
            assertNotNull(context.getBean(TraceIdFilter.class));
            Operations proxy = context.getBean(Operations.class);
            Logger logger = (Logger) LoggerFactory.getLogger(OperationLogAspect.class);
            ListAppender<ILoggingEvent> appender = new ListAppender<>();
            appender.start();
            logger.addAppender(appender);
            try {
                assertEquals("result", proxy.success());
                assertSame(target.failure, assertThrows(IllegalStateException.class, proxy::fail));
                assertEquals(2, appender.list.size());
                assertTrue(appender.list.get(0).getFormattedMessage().contains("success=true"));
                assertTrue(appender.list.get(1).getFormattedMessage().contains("success=false"));
            } finally {
                logger.detachAppender(appender);
                appender.stop();
            }
        }
    }
    @Test void propagatesTraceIdAndCleansUpAfterFailure() {
        var request = new MockHttpServletRequest();
        request.addHeader("traceId", "request-123");
        var response = new MockHttpServletResponse();
        var original = new IllegalStateException("request failed");
        assertSame(original, assertThrows(IllegalStateException.class, () ->
            new TraceIdFilter().doFilter(request, response, (req, res) -> {
                assertEquals("request-123", MDC.get("traceId"));
                throw original;
            })));
        assertEquals("request-123", response.getHeader("traceId"));
        assertNull(MDC.get("traceId"));
    }
}
