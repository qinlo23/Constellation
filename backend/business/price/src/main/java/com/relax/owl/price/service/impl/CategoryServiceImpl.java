package com.relax.owl.price.service.impl;

import com.relax.owl.price.domain.entity.CategoryDO;
import com.relax.owl.price.mapper.CategoryMapper;
import com.relax.owl.price.service.CategoryService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 价格系统分类表 服务实现类
 * </p>
 *
 * @author slnt23
 * @since 2026-04-12 20:43:32
 */
@Service("priceCategoryServiceImpl")
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, CategoryDO> implements CategoryService {

}
