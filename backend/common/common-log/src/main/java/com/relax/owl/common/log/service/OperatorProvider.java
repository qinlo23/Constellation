package com.relax.owl.common.log.service;

/** Optional application adapter; anonymous operations have a null operator. */
@FunctionalInterface
public interface OperatorProvider {
    Long currentOperatorId();
}
