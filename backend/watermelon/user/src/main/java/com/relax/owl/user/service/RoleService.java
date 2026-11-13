package com.relax.owl.user.service;

import com.relax.owl.user.domain.dto.RoleCreateCommand;
import com.relax.owl.user.domain.dto.RoleUpdateCommand;
import com.relax.owl.user.domain.vo.RoleInfo;

import java.util.List;

/**
 * <p>
 * 角色表 服务类
 * </p>
 *
 * @author slnt23
 * @since 2026-04-13 23:53:18
 */
public interface RoleService {

    List<RoleInfo> listAll();

    RoleInfo getById(Long id);

    Long create(RoleCreateCommand createDTO);

    Boolean update(Long id, RoleUpdateCommand updateDTO);

    Boolean deleteById(Long id);

    Boolean updateEnabled(Long id, Boolean enabled);

}
