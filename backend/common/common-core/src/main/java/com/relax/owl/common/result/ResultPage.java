package com.relax.owl.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页统一包装类，
 *
 * @author slnt23
 * @since 2025/11/18
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultPage<T> {

    private Long currentPage;

    private Long pageSize;

    private Long total;

    private Long totalPage;

    private List<T> records;

}
