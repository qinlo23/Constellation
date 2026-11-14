package com.relax.owl.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.relax.owl.user.domain.dto.*;
import com.relax.owl.user.domain.entity.RoleDO;
import com.relax.owl.user.domain.vo.RoleInfo;
import com.relax.owl.user.mapper.RoleMapper;
import com.relax.owl.user.service.*;
import com.relax.owl.common.exception.BizException;
import com.relax.owl.common.result.ResultStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
@Transactional(rollbackFor = Exception.class)
public class RoleServiceImpl implements RoleService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RoleServiceImpl.class);
    private final RoleMapper roleMapper;
    private final AuthorizationService authorization;
    public List<RoleInfo> listAll() {
        return roleMapper.selectList(Wrappers.<RoleDO>lambdaQuery().orderByAsc(RoleDO::getId)).stream().map(this::toVO).toList();
    }
    public RoleInfo getById(Long id) { return toVO(authorization.requireRole(id)); }
    public Long create(RoleCreateCommand command) {
        authorization.lock();
        if (command.getRoleCode() == null || !command.getRoleCode().matches("[A-Z][A-Z0-9_]{1,63}"))
            throw new BizException(ResultStatus.PARAMS_INVALID);
        RoleDO role = new RoleDO();
        role.setRoleCode(command.getRoleCode());
        role.setRoleName(command.getRoleName().trim());
        role.setDescription(command.getDescription());
        role.setEnabled(!Boolean.FALSE.equals(command.getEnabled()));
        role.setBuiltIn(false);
        roleMapper.insert(role);
        audit(role.getId(), null, role);
        return role.getId();
    }
    public Boolean update(Long id, RoleUpdateCommand command) {
        authorization.lock();
        RoleDO role = authorization.requireRole(id);
        String before = role.toString();
        if (command.getEnabled() != null && !command.getEnabled().equals(role.getEnabled())) {
            checkMutable(role);
            role.setEnabled(command.getEnabled());
            authorization.bumpRole(id);
        }
        if (command.getRoleName() != null && !command.getRoleName().isBlank()) role.setRoleName(command.getRoleName().trim());
        if (command.getDescription() != null) role.setDescription(command.getDescription());
        boolean updated = roleMapper.updateById(role) > 0;
        if (updated) audit(id, before, role);
        return updated;
    }
    public Boolean deleteById(Long id) {
        authorization.lock();
        RoleDO role = authorization.requireRole(id);
        checkMutable(role);
        authorization.bumpRole(id);
        boolean deleted = roleMapper.deleteById(id) > 0;
        if (deleted) audit(id, role, null);
        return deleted;
    }
    public Boolean updateEnabled(Long id, Boolean enabled) {
        RoleUpdateCommand command = new RoleUpdateCommand();
        command.setEnabled(enabled);
        return update(id, command);
    }
    private void audit(Long id, Object before, Object after) {
        Long operator = com.relax.owl.common.security.CurrentUserContext.getUserId();
        String previous = String.valueOf(before);
        String next = String.valueOf(after);
        if (org.springframework.transaction.support.TransactionSynchronizationManager.isSynchronizationActive()) {
            org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    public void afterCommit() {
                        log.info("authorization operator={} role={} before={} after={} success=true", operator, id, previous, next);
                    }
                });
        }
    }
    private void checkMutable(RoleDO role) {
        if (Boolean.TRUE.equals(role.getBuiltIn())) throw new BizException(ResultStatus.ROLE_IN_USE);
    }
    private RoleInfo toVO(RoleDO role) {
        RoleInfo result = new RoleInfo();
        BeanUtils.copyProperties(role, result);
        return result;
    }
}
