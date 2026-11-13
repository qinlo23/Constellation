package com.relax.owl.common.exception;

import com.relax.owl.common.result.Result;
import com.relax.owl.common.result.ResultStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Last resort after all specific exception mappings. */
@Slf4j
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class FallbackExceptionHandler {
    /**
     * 全局通用异常处理，兜底
     * @param e 异常类型，
     * @return 回显数据
     */
    @ExceptionHandler(value = Exception.class)
    public Result<?> exceptionHandler(Exception e) {

        log.error("发生错误，未捕获具体异常，报错信息：{}",e.getMessage(),e);
        return Result.fail(ResultStatus.FAIL);
    }
}
