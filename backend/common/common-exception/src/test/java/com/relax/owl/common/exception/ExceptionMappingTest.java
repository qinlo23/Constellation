package com.relax.owl.common.exception;

import com.relax.owl.common.result.ResultStatus;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ExceptionMappingTest {
    @RestController
    static class Failures {
        @GetMapping("/business") Object business() { throw new BizException(ResultStatus.DATA_ALREADY_EXIST); }
        @GetMapping("/duplicate") Object duplicate() { throw new DuplicateKeyException("duplicate"); }
        @GetMapping("/denied") Object denied() { throw new AccessDeniedException("denied"); }
        @GetMapping("/unexpected") Object unexpected() { throw new IllegalStateException("internal"); }
    }
    @Test void specializedHandlersWinOverFallbackAndPreserveResponseCodes() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new Failures())
            .setControllerAdvice(new GlobalExceptionHandler(), new FallbackExceptionHandler()).build();
        mvc.perform(get("/business")).andExpect(jsonPath("$.code").value(ResultStatus.DATA_ALREADY_EXIST.getCode()));
        mvc.perform(get("/duplicate")).andExpect(jsonPath("$.code").value(ResultStatus.DATA_ALREADY_EXIST.getCode()));
        mvc.perform(get("/denied")).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(ResultStatus.FORBIDDEN.getCode()));
        mvc.perform(get("/unexpected")).andExpect(jsonPath("$.code").value(ResultStatus.FAIL.getCode()));
    }
}
