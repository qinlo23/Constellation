package com.relax.owl.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import com.relax.owl.user.domain.entity.RoleDO;

/**
 * 共享角色 Mapper，登录校验与后台角色管理共用。
 */
@Mapper
public interface RoleMapper extends BaseMapper<RoleDO> {
}
