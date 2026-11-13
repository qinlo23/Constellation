package com.relax.owl.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.relax.owl.user.domain.dto.RoleCreateCommand;
import com.relax.owl.user.domain.dto.RoleUpdateCommand;
import com.relax.owl.user.domain.entity.UserDO;
import com.relax.owl.user.domain.vo.RoleInfo;
import com.relax.owl.user.mapper.RoleMapper;
import com.relax.owl.user.mapper.UserMapper;
import com.relax.owl.user.service.RoleService;
import com.relax.owl.user.domain.entity.RoleDO;
import com.relax.owl.common.exception.BizException;
import com.relax.owl.common.result.ResultStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 角色表 服务实现类
 * </p>
 *
 * @author slnt23
 * @since 2026-04-13 23:53:18
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final UserMapper userMapper;

    @Override
    public List<RoleInfo> listAll() {
        List<RoleDO> roleDOS = roleMapper.selectList(
                Wrappers.<RoleDO>lambdaQuery().orderByAsc(RoleDO::getId));
        return roleDOS.stream().map(this::toVO).toList();
    }

    @Override
    public RoleInfo getById(Long id) {
        return toVO(requireRole(id));
    }

    @Override
    public Long create(RoleCreateCommand createDTO) {
        String roleName = createDTO.getRoleName().trim();
        if (existsByRoleName(roleName, null)) {
            throw new BizException(ResultStatus.DATA_ALREADY_EXIST);
        }

        RoleDO roleDO = new RoleDO();
        roleDO.setRoleName(roleName);
        roleDO.setDescription(createDTO.getDescription());
        roleDO.setEnabled(createDTO.getEnabled() == null || createDTO.getEnabled());
        roleDO.setCreateTime(LocalDateTime.now());
        roleDO.setUpdateTime(LocalDateTime.now());
        roleMapper.insert(roleDO);
        return roleDO.getId();
    }

    @Override
    public Boolean update(Long id, RoleUpdateCommand updateDTO) {
        requireRole(id);

        LambdaUpdateWrapper<RoleDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(RoleDO::getId, id);
        boolean changed = false;

        if (updateDTO.getRoleName() != null && !updateDTO.getRoleName().isBlank()) {
            String roleName = updateDTO.getRoleName().trim();
            if (existsByRoleName(roleName, id)) {
                throw new BizException(ResultStatus.DATA_ALREADY_EXIST);
            }
            wrapper.set(RoleDO::getRoleName, roleName);
            changed = true;
        }
        if (updateDTO.getDescription() != null) {
            wrapper.set(RoleDO::getDescription, updateDTO.getDescription().trim());
            changed = true;
        }
        if (updateDTO.getEnabled() != null) {
            checkRoleCanDisable(id, updateDTO.getEnabled());
            wrapper.set(RoleDO::getEnabled, updateDTO.getEnabled());
            changed = true;
        }

        return !changed || roleMapper.update(null, wrapper) > 0;
    }

    @Override
    public Boolean deleteById(Long id) {
        RoleDO roleDO = requireRole(id);
        if (isRoleInUse(roleDO.getRoleName())) {
            throw new BizException(ResultStatus.ROLE_IN_USE);
        }
        return roleMapper.deleteById(id) > 0;
    }

    @Override
    public Boolean updateEnabled(Long id, Boolean enabled) {
        requireRole(id);
        checkRoleCanDisable(id, enabled);

        LambdaUpdateWrapper<RoleDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(RoleDO::getId, id).set(RoleDO::getEnabled, enabled);
        return roleMapper.update(null, wrapper) > 0;
    }

    private RoleDO requireRole(Long id) {
        RoleDO roleDO = roleMapper.selectById(id);
        if (roleDO == null) {
            throw new BizException(ResultStatus.NOT_FOUND);
        }
        return roleDO;
    }

    private RoleInfo toVO(RoleDO roleDO) {
        RoleInfo roleVO = new RoleInfo();
        roleVO.setId(roleDO.getId());
        roleVO.setRoleName(roleDO.getRoleName());
        roleVO.setDescription(roleDO.getDescription());
        roleVO.setEnabled(roleDO.getEnabled());
        roleVO.setCreateTime(roleDO.getCreateTime());
        roleVO.setUpdateTime(roleDO.getUpdateTime());
        return roleVO;
    }

    private boolean existsByRoleName(String roleName, Long excludeId) {
        LambdaQueryWrapper<RoleDO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(RoleDO::getRoleName, roleName);
        if (excludeId != null) {
            wrapper.ne(RoleDO::getId, excludeId);
        }
        return roleMapper.selectCount(wrapper) > 0;
    }

    private boolean isRoleInUse(String roleName) {
        LambdaQueryWrapper<UserDO> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(UserDO::getRoleName, roleName);
        return userMapper.selectCount(wrapper) > 0;
    }

    private void checkRoleCanDisable(Long id, Boolean enabled) {
        RoleDO roleDO = requireRole(id);
        if (Boolean.FALSE.equals(enabled) && isRoleInUse(roleDO.getRoleName())) {
            throw new BizException(ResultStatus.ROLE_IN_USE);
        }
    }

}
