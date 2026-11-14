package com.relax.owl.user.service;

import com.relax.owl.common.security.AuthorizationProvider;
import com.relax.owl.common.exception.BizException;
import com.relax.owl.common.result.ResultStatus;
import com.relax.owl.common.security.CurrentUserContext;
import com.relax.owl.common.log.annotation.OperationLog;
import com.relax.owl.common.log.constant.LogType;
import com.relax.owl.user.domain.entity.RoleDO;
import com.relax.owl.user.domain.vo.PermissionInfo;
import com.relax.owl.user.mapper.AuthorizationMapper;
import com.relax.owl.user.mapper.RoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthorizationService implements AuthorizationProvider {
    private final AuthorizationMapper mapper;
    private final RoleMapper roleMapper;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthorizationService.class);

    @Transactional(readOnly = true, isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public Authorization load(Long userId) {
        if (userId == null) return null;
        var user = mapper.account(userId);
        if (user == null) return null;
        var roles = mapper.roles(userId).stream().filter(r -> Boolean.TRUE.equals(r.getEnabled()))
                .map(RoleDO::getRoleCode).toList();
        var permissions = roles.contains("SUPER_ADMIN")
                ? mapper.catalog().stream().map(PermissionInfo::getCode).toList() : mapper.permissions(userId);
        return new Authorization(user.getAuthVersion(), Objects.equals(user.getStatus(), 0), roles, permissions);
    }

    public List<RoleDO> roles(Long userId) { return mapper.roles(userId); }
    public List<PermissionInfo> catalog() { return mapper.catalog(); }
    public List<String> rolePermissions(Long id) {
        requireRole(id);
        return mapper.rolePermissions(id);
    }
    public RoleDO requireRole(Long id) {
        var role = roleMapper.selectById(id);
        if (role == null) throw new BizException(ResultStatus.NOT_FOUND);
        return role;
    }
    public void lock() {
        if (mapper.lockAdministration() == null) throw new IllegalStateException("SUPER_ADMIN role missing");
    }
    /** Caller must hold the administration lock in the surrounding transaction. */
    public void protectLastSuperAdmin(Long id) {
        var user = mapper.account(id);
        if (user == null) throw new BizException(ResultStatus.NOT_FOUND);
        if (Objects.equals(user.getStatus(), 0) && mapper.roles(id).stream()
                .anyMatch(r -> "SUPER_ADMIN".equals(r.getRoleCode())) && mapper.activeSuperAdmins() <= 1) {
            throw new BizException(ResultStatus.ROLE_IN_USE);
        }
    }
    /** Prevent credential/status/profile operations from becoming an indirect super-admin grant. */
    public void guardAccountMutation(Long id) {
        boolean superAdmin = com.relax.owl.common.security.PermissionChecks.has("ROLE_SUPER_ADMIN");
        if (!superAdmin && mapper.roles(id).stream().anyMatch(r -> "SUPER_ADMIN".equals(r.getRoleCode())))
            throw new org.springframework.security.access.AccessDeniedException("Only SUPER_ADMIN may modify a super administrator");
    }
    @Transactional(rollbackFor = Exception.class)
    public void initializeDefaultRole(Long userId) {
        lock();
        if (mapper.account(userId) == null || !mapper.roles(userId).isEmpty())
            throw new BizException(ResultStatus.PARAMS_INVALID);
        var role = roleMapper.selectOne(com.baomidou.mybatisplus.core.toolkit.Wrappers.<RoleDO>lambdaQuery()
                .eq(RoleDO::getRoleCode, "USER").eq(RoleDO::getEnabled, true));
        if (role == null) throw new BizException(ResultStatus.ROLE_FAILED);
        mapper.assign(userId, role.getId());
        audit("user", userId, List.of(), List.of(role.getId()));
    }
    public void bumpUser(Long id) { mapper.bumpUser(id); }
    public void bumpRole(Long id) { mapper.bumpRole(id); }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Transactional(rollbackFor = Exception.class)
    @OperationLog(type = LogType.ADMIN, module = "授权管理", action = "分配用户角色")
    public void replaceRoles(Long userId, List<Long> roleIds) {
        lock();
        if (mapper.account(userId) == null) throw new BizException(ResultStatus.NOT_FOUND);
        if (roleIds == null || roleIds.isEmpty() || roleIds.stream().anyMatch(Objects::isNull))
            throw new BizException(ResultStatus.PARAMS_INVALID);
        var ids = new TreeSet<>(roleIds);
        var selected = ids.stream().map(this::requireRole).toList();
        if (selected.stream().anyMatch(r -> !Boolean.TRUE.equals(r.getEnabled())))
            throw new BizException(ResultStatus.ROLE_FAILED);
        if (selected.stream().noneMatch(r -> "SUPER_ADMIN".equals(r.getRoleCode()))) protectLastSuperAdmin(userId);
        var before = mapper.roles(userId).stream().map(RoleDO::getId).toList();
        if (new TreeSet<>(before).equals(ids)) return;
        mapper.clearRoles(userId);
        ids.forEach(id -> mapper.assign(userId, id));
        mapper.bumpUser(userId);
        audit("user", userId, before, ids);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Transactional(rollbackFor = Exception.class)
    @OperationLog(type = LogType.ADMIN, module = "授权管理", action = "配置角色权限")
    public void replacePermissions(Long roleId, List<String> codes) {
        lock();
        var role = requireRole(roleId);
        if (Boolean.TRUE.equals(role.getBuiltIn()) || codes == null || codes.stream().anyMatch(Objects::isNull))
            throw new BizException(ResultStatus.PARAMS_INVALID);
        var selected = new TreeSet<>(codes);
        var known = mapper.catalog().stream().map(PermissionInfo::getCode).collect(java.util.stream.Collectors.toSet());
        if (!known.containsAll(selected)) throw new BizException(ResultStatus.PARAMS_INVALID);
        // An operation requires its corresponding page; all admin permissions require entry access.
        for (String code : List.copyOf(selected)) {
            String read = code.substring(0, code.indexOf(':')) + ":read";
            if (known.contains(read)) selected.add(read);
        }
        if (!selected.isEmpty()) selected.add("admin:access");
        var before = mapper.rolePermissions(roleId);
        if (new TreeSet<>(before).equals(selected)) return;
        mapper.clearPermissions(roleId);
        selected.forEach(code -> mapper.grant(roleId, code));
        mapper.bumpRole(roleId);
        audit("role-permissions", roleId, before, selected);
    }
    private void audit(String targetType, Long id, Object before, Object after) {
        Long operator = CurrentUserContext.getUserId();
        if (org.springframework.transaction.support.TransactionSynchronizationManager.isSynchronizationActive()) {
            org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    public void afterCommit() {
                        log.info("authorization operator={} targetType={} target={} before={} after={} success=true", operator, targetType, id, before, after);
                    }
                });
        }
    }

}
