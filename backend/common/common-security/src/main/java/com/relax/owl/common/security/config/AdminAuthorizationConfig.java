package com.relax.owl.common.security.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** New admin handlers must explicitly declare an authorization policy. */
@Configuration
public class AdminAuthorizationConfig implements WebMvcConfigurer {
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                String path = request.getRequestURI().substring(request.getContextPath().length());
                if ("GET".equals(request.getMethod())
                        && (path.equals("/api/admin/spotlight") || path.equals("/api/admin/feature"))) return true;
                if (!(handler instanceof HandlerMethod method)
                        || (!AnnotatedElementUtils.hasAnnotation(method.getMethod(), PreAuthorize.class)
                        && !AnnotatedElementUtils.hasAnnotation(method.getBeanType(), PreAuthorize.class))) {
                    throw new AccessDeniedException("Admin endpoint has no authorization policy");
                }
                return true;
            }
        }).addPathPatterns("/api/admin/**");
    }
}
