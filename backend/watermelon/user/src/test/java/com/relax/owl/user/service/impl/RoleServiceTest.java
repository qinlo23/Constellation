package com.relax.owl.user.service.impl;

import com.relax.owl.user.domain.entity.RoleDO;
import com.relax.owl.user.domain.dto.RoleCreateCommand;
import com.relax.owl.user.domain.dto.RoleUpdateCommand;
import com.relax.owl.user.mapper.RoleMapper;
import com.relax.owl.user.mapper.UserMapper;
import com.relax.owl.common.exception.BizException;
import com.relax.owl.common.result.ResultStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

class RoleServiceTest {
    private RoleMapper roles;
    private UserMapper users;
    private RoleServiceImpl service;
    @BeforeEach void setup() {
        roles = mock(RoleMapper.class);
        users = mock(UserMapper.class);
        service = new RoleServiceImpl(roles, users);
    }
    private RoleDO role() {
        RoleDO role = new RoleDO();
        role.setId(1L);
        role.setRoleName("USER");
        role.setEnabled(true);
        return role;
    }
    @Test void rejectsMissingRole() {
        var error = assertThrows(BizException.class, () -> service.getById(1L));
        assertEquals(ResultStatus.NOT_FOUND.getCode(), error.getCode());
    }
    @Test void rejectsDuplicateRoleCreation() {
        when(roles.selectCount(any())).thenReturn(1L);
        RoleCreateCommand command = new RoleCreateCommand();
        command.setRoleName(" USER ");
        var error = assertThrows(BizException.class, () -> service.create(command));
        assertEquals(ResultStatus.DATA_ALREADY_EXIST.getCode(), error.getCode());
    }
    @Test void preventsDeletingOrDisablingUsedRole() {
        when(roles.selectById(1L)).thenReturn(role());
        when(users.selectCount(any())).thenReturn(1L);
        assertEquals(ResultStatus.ROLE_IN_USE.getCode(),
                assertThrows(BizException.class, () -> service.deleteById(1L)).getCode());
        assertEquals(ResultStatus.ROLE_IN_USE.getCode(),
                assertThrows(BizException.class, () -> service.updateEnabled(1L, false)).getCode());
        verify(roles, never()).deleteById(1L);
    }
    @Test void emptyPatchSucceedsWithoutWriting() {
        when(roles.selectById(1L)).thenReturn(role());
        assertTrue(service.update(1L, new RoleUpdateCommand()));
        verify(roles).selectById(1L);
        verifyNoMoreInteractions(roles);
    }
    @Test void returnsDtoInsteadOfPersistenceEntity() {
        when(roles.selectById(1L)).thenReturn(role());
        var result = service.getById(1L);
        assertEquals("USER", result.getRoleName());
        assertEquals(1L, result.getId().longValue());
        assertTrue(result.getEnabled());
    }
}
