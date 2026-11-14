package com.relax.owl.user.service.impl;

import com.relax.owl.user.domain.entity.RoleDO;
import com.relax.owl.user.domain.dto.RoleCreateCommand;
import com.relax.owl.user.mapper.RoleMapper;
import com.relax.owl.user.service.AuthorizationService;
import com.relax.owl.common.exception.BizException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoleServiceTest {
    private RoleMapper roles;
    private AuthorizationService authorization;
    private RoleServiceImpl service;
    @BeforeEach void setup() {
        roles = mock(RoleMapper.class);
        authorization = mock(AuthorizationService.class);
        service = new RoleServiceImpl(roles, authorization);
    }
    private RoleDO role(boolean builtIn) {
        RoleDO role = new RoleDO();
        role.setId(1L);
        role.setRoleCode(builtIn ? "USER" : "EDITOR");
        role.setRoleName("展示名称");
        role.setEnabled(true);
        role.setBuiltIn(builtIn);
        when(authorization.requireRole(1L)).thenReturn(role);
        return role;
    }
    @Test void rejectsInvalidCodeBeforeInsertion() {
        RoleCreateCommand command = new RoleCreateCommand();
        command.setRoleCode("display name");
        command.setRoleName("展示名称");
        assertThrows(BizException.class, () -> service.create(command));
        verifyNoInteractions(roles);
    }
    @Test void protectsBuiltInRoleLifecycle() {
        role(true);
        assertThrows(BizException.class, () -> service.deleteById(1L));
        assertThrows(BizException.class, () -> service.updateEnabled(1L, false));
        verify(roles, never()).deleteById(1L);
        verify(authorization, never()).bumpRole(anyLong());
    }
    @Test void disablingAssignedCustomRoleInvalidatesMembers() {
        role(false);
        when(roles.updateById(any(RoleDO.class))).thenReturn(1);
        assertTrue(service.updateEnabled(1L, false));
        verify(authorization).bumpRole(1L);
        verify(roles).updateById(argThat((RoleDO r) -> !r.getEnabled()));
    }
    @Test void deletingCustomRoleInvalidatesBeforeCascade() {
        role(false);
        when(roles.deleteById(1L)).thenReturn(1);
        assertTrue(service.deleteById(1L));
        var order = inOrder(authorization, roles);
        order.verify(authorization).lock();
        order.verify(authorization).requireRole(1L);
        order.verify(authorization).bumpRole(1L);
        order.verify(roles).deleteById(1L);
    }
    @Test void displayNameDoesNotBecomeIdentity() {
        role(true);
        var result = service.getById(1L);
        assertEquals("展示名称", result.getRoleName());
        assertEquals("USER", result.getRoleCode());
        assertTrue(result.getBuiltIn());
    }
}
