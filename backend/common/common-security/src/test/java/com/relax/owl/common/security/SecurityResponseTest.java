package com.relax.owl.common.security;

import com.relax.owl.common.security.handler.RestAccessDeniedHandler;
import com.relax.owl.common.security.handler.RestAuthenticationEntryPoint;
import com.relax.owl.common.security.filter.JwtAuthenticationFilter;
import com.relax.owl.common.result.ResultStatus;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import static org.junit.jupiter.api.Assertions.*;

class SecurityResponseTest {
    @Test void writesResponsesWithoutCommonWeb() throws Exception {
        try (var context = new AnnotationConfigApplicationContext(SecurityJsonConfig.class,
                RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class)) {
            var request = new MockHttpServletRequest();
            request.setAttribute(JwtAuthenticationFilter.JWT_ERROR_STATUS_ATTRIBUTE, ResultStatus.TOKEN_EXPIRED);
            var unauthorized = new MockHttpServletResponse();
            context.getBean(RestAuthenticationEntryPoint.class).commence(request, unauthorized,
                    new BadCredentialsException("expired"));
            assertEquals(401, unauthorized.getStatus());
            assertTrue(unauthorized.getContentAsString().contains("401001"));
            var forbidden = new MockHttpServletResponse();
            context.getBean(RestAccessDeniedHandler.class).handle(request, forbidden, new AccessDeniedException("denied"));
            assertEquals(403, forbidden.getStatus());
            assertTrue(forbidden.getContentAsString().contains("403"));
        }
    }
}
