package com.relax.owl.common.mybatis;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MybatisPageConverterTest {
    @Test void preservesPaginationAndRecords() {
        Page<String> source = new Page<>(2, 10, 23);
        source.setRecords(List.of("item"));
        var result = MybatisPageConverter.toResultPage(source);
        assertEquals(2L, result.getCurrentPage().longValue());
        assertEquals(10L, result.getPageSize().longValue());
        assertEquals(23L, result.getTotal().longValue());
        assertEquals(3L, result.getTotalPage().longValue());
        assertEquals(source.getRecords(), result.getRecords());
    }
    @Test void convertsEmptyPage() {
        var result = MybatisPageConverter.toResultPage(new Page<String>(1, 10, 0));
        assertEquals(0L, result.getTotal().longValue());
        assertEquals(0L, result.getTotalPage().longValue());
        assertTrue(result.getRecords().isEmpty());
    }
}
