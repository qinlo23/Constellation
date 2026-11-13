package com.relax.owl.common.mybatis;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.relax.owl.common.result.ResultPage;

/** Converts persistence pagination into the public response model. */
public final class MybatisPageConverter {
    private MybatisPageConverter() {}

    public static <T> ResultPage<T> toResultPage(IPage<T> page) {
        return new ResultPage<>(page.getCurrent(), page.getSize(), page.getTotal(),
                page.getPages(), page.getRecords());
    }
}
