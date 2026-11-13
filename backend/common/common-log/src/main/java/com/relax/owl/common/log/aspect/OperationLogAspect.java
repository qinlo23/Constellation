package com.relax.owl.common.log.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import com.relax.owl.common.log.service.OperatorProvider;

import com.relax.owl.common.log.annotation.OperationLog;
import com.relax.owl.common.log.domain.message.OperationLogMessage;
import com.relax.owl.common.log.filter.TraceIdFilter;


@Aspect
@Slf4j
@RequiredArgsConstructor
public class OperationLogAspect {

    private final ObjectProvider<OperatorProvider> operatorProvider;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        Long operatorId = currentOperatorId();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String method = signature.getDeclaringType().getSimpleName()
                + "#" + signature.getName();

        boolean success = false;
        String errorMsg = null;
        try {
            Object result = joinPoint.proceed();
            success = true;
            return result;
        } catch (Throwable e) {
            errorMsg = e.getMessage();
            throw e;
        } finally {
            long cost = System.currentTimeMillis() - startTime;
            writeLog(operationLog, operatorId, method, success, cost, errorMsg);
        }
    }

    private void writeLog(OperationLog operationLog, Long operatorId,
                             String method, boolean success, long cost, String errorMsg) {
        OperationLogMessage message = new OperationLogMessage();
        message.setLogType(operationLog.type());
        message.setModule(operationLog.module());
        message.setAction(operationLog.action());
        message.setOperatorId(operatorId);
        message.setMethod(method);
        message.setSuccess(success);
        message.setCost(cost);
        message.setErrorMsg(errorMsg);
        message.setTraceId(MDC.get(TraceIdFilter.TRACE_ID));

        try {
            log.info("[human] operation {}", message);
        } catch (RuntimeException e) {
            // Logging must not change the outcome of the intercepted operation.
        }
    }

    private Long currentOperatorId() {
        try {
            OperatorProvider provider = operatorProvider.getIfAvailable();
            return provider == null ? null : provider.currentOperatorId();
        } catch (RuntimeException e) {
            return null;
        }
    }
}
