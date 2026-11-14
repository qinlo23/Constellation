package com.relax.owl.user.service.impl;

import com.relax.owl.user.domain.entity.*;
import com.relax.owl.user.domain.vo.PermissionInfo;
import com.relax.owl.user.mapper.*;
import com.relax.owl.user.service.AuthorizationService;
import com.relax.owl.common.exception.BizException;
import org.junit.jupiter.api.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthorizationServiceTest {
    AuthorizationMapper mapper;
    RoleMapper roles;
    AuthorizationService service;
    @BeforeEach void setup() {
        mapper = mock(AuthorizationMapper.class);
        roles = mock(RoleMapper.class);
        service = new AuthorizationService(mapper, roles);
        when(mapper.lockAdministration()).thenReturn(1L);
    }
    @AfterEach void cleanup() { SecurityContextHolder.clearContext(); }
    RoleDO role(long id, String code, boolean enabled, boolean builtIn) {
        var role = new RoleDO(); role.setId(id); role.setRoleCode(code);
        role.setEnabled(enabled); role.setBuiltIn(builtIn);
        when(roles.selectById(id)).thenReturn(role);
        return role;
    }
    void user(long id) {
        UserDO user = new UserDO(); user.setId(id); user.setStatus(0); user.setAuthVersion(4L);
        when(mapper.account(id)).thenReturn(user);
    }
    PermissionInfo permission(String code) {
        PermissionInfo info = new PermissionInfo(); info.setCode(code); return info;
    }
    @Test void loadsMultipleEnabledRolesAndUnionOfPermissions() {
        user(8L);
        doReturn(List.of(role(1,"USER",true,true), role(2,"EDITOR",true,false), role(3,"DISABLED",false,false))).when(mapper).roles(8L);
        when(mapper.permissions(8L)).thenReturn(List.of("admin:access","feature:read"));
        var result = service.load(8L);
        assertEquals(List.of("USER","EDITOR"), result.roles());
        assertEquals(List.of("admin:access","feature:read"), result.permissions());
        assertEquals(4, result.version());
    }
    @Test void superAdminGetsCodeOwnedCatalog() {
        user(8L);
        doReturn(List.of(role(1,"SUPER_ADMIN",true,true))).when(mapper).roles(8L);
        when(mapper.catalog()).thenReturn(List.of(permission("admin:access"),permission("feature:read")));
        assertEquals(List.of("admin:access","feature:read"),service.load(8L).permissions());
        verify(mapper, never()).permissions(anyLong());
    }
    @Test void protectsLastActiveSuperAdmin() {
        user(8L);
        doReturn(List.of(role(1,"SUPER_ADMIN",true,true))).when(mapper).roles(8L);
        when(mapper.activeSuperAdmins()).thenReturn(1L);
        assertThrows(BizException.class, () -> service.protectLastSuperAdmin(8L));
        verify(mapper, never()).clearRoles(anyLong());
    }
    @Test void cannotRemoveLastSuperAdminMembership() {
        user(8L);
        role(2,"USER",true,true);
        doReturn(List.of(role(1,"SUPER_ADMIN",true,true))).when(mapper).roles(8L);
        when(mapper.activeSuperAdmins()).thenReturn(1L);
        assertThrows(BizException.class, () -> service.replaceRoles(8L,List.of(2L)));
        verify(mapper, never()).clearRoles(anyLong());
    }
    @Test void replacesRolesWithoutDuplicatesAndInvalidatesOldSessions() {
        user(8L);
        role(2,"EDITOR",true,false);
        doReturn(List.of(role(1,"USER",true,true))).when(mapper).roles(8L);
        service.replaceRoles(8L,List.of(2L,2L));
        var order=inOrder(mapper);
        order.verify(mapper).lockAdministration();
        order.verify(mapper).clearRoles(8L);
        order.verify(mapper).assign(8L,2L);
        order.verify(mapper).bumpUser(8L);
    }
    @Test void rejectsDisabledRolesBeforeClearingMembership() {
        user(8L); role(2,"EDITOR",false,false);
        assertThrows(BizException.class, () -> service.replaceRoles(8L,List.of(2L)));
        verify(mapper, never()).clearRoles(anyLong());
    }
    @Test void normalizesOperationPermissionsAndInvalidatesAllMembers() {
        role(2,"EDITOR",true,false);
        when(mapper.catalog()).thenReturn(List.of(permission("feature:update"),permission("feature:read"),permission("admin:access")));
        service.replacePermissions(2L,List.of("feature:update"));
        verify(mapper).grant(2L,"feature:update");
        verify(mapper).grant(2L,"feature:read");
        verify(mapper).grant(2L,"admin:access");
        verify(mapper).bumpRole(2L);
    }
    @Test void rejectsUnknownPermissionAndBuiltInRoleChanges() {
        role(2,"EDITOR",true,false);
        assertThrows(BizException.class, () -> service.replacePermissions(2L,List.of("made-up:grant")));
        role(1,"USER",true,true);
        assertThrows(BizException.class, () -> service.replacePermissions(1L,List.of()));
        verify(mapper, never()).clearPermissions(anyLong());
    }
    @Test void nonSuperAdminCannotModifySuperAdminCredentials() {
        doReturn(List.of(role(1,"SUPER_ADMIN",true,true))).when(mapper).roles(8L);
        assertThrows(AccessDeniedException.class, () -> service.guardAccountMutation(8L));
    }
}
