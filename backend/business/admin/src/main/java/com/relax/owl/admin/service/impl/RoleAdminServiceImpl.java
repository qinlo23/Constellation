package com.relax.owl.admin.service.impl;

import com.relax.owl.admin.domain.dto.RoleCreateDTO;
import com.relax.owl.admin.domain.dto.RoleUpdateDTO;
import com.relax.owl.admin.domain.vo.RoleVO;
import com.relax.owl.admin.service.RoleAdminService;
import com.relax.owl.user.domain.dto.RoleCreateCommand;
import com.relax.owl.user.domain.dto.RoleUpdateCommand;
import com.relax.owl.user.domain.vo.RoleInfo;
import com.relax.owl.user.service.RoleService;
import com.relax.owl.common.log.annotation.OperationLog;
import com.relax.owl.common.log.constant.LogType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import java.util.List;

/** Preserves the administration API while user owns role persistence. */
@Service
@RequiredArgsConstructor
public class RoleAdminServiceImpl implements RoleAdminService {
    private final RoleService roleService;

    public List<RoleVO> listAll() { return roleService.listAll().stream().map(this::toVO).toList(); }
    public RoleVO getById(Long id) { return toVO(roleService.getById(id)); }

    @OperationLog(type = LogType.ADMIN, module = "角色管理", action = "新增角色")
    public Long create(RoleCreateDTO dto) {
        RoleCreateCommand command = new RoleCreateCommand();
        BeanUtils.copyProperties(dto, command);
        return roleService.create(command);
    }
    @OperationLog(type = LogType.ADMIN, module = "角色管理", action = "更新角色")
    public Boolean update(Long id, RoleUpdateDTO dto) {
        RoleUpdateCommand command = new RoleUpdateCommand();
        BeanUtils.copyProperties(dto, command);
        return roleService.update(id, command);
    }
    @OperationLog(type = LogType.ADMIN, module = "角色管理", action = "删除角色")
    public Boolean deleteById(Long id) { return roleService.deleteById(id); }
    @OperationLog(type = LogType.ADMIN, module = "角色管理", action = "启用/禁用角色")
    public Boolean updateEnabled(Long id, Boolean enabled) { return roleService.updateEnabled(id, enabled); }

    private RoleVO toVO(RoleInfo info) {
        RoleVO result = new RoleVO();
        BeanUtils.copyProperties(info, result);
        return result;
    }
}
