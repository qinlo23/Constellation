package com.relax.owl.admin.service;

import com.relax.owl.admin.service.impl.UserAdminServiceImpl;
import com.relax.owl.admin.domain.entity.UserDO;
import com.relax.owl.admin.domain.dto.*;
import com.relax.owl.admin.mapper.UserAdminMapper;
import com.relax.owl.admin.convert.UserAdminConvert;
import com.relax.owl.user.mapper.*;
import com.relax.owl.user.service.AuthorizationService;
import com.relax.owl.common.exception.BizException;
import com.relax.owl.common.result.ResultStatus;
import com.relax.owl.common.security.TokenRevocationService;
import org.junit.jupiter.api.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserAuthorizationBoundaryTest {
    UserAdminMapper users;
    AuthorizationService authorization;
    UserAdminServiceImpl service;
    @BeforeEach void setup() {
        users=mock(UserAdminMapper.class); authorization=mock(AuthorizationService.class);
        service=new UserAdminServiceImpl(users,authorization,
                mock(UserAdminConvert.class),mock(PasswordEncoder.class),mock(TokenRevocationService.class));
        var user=new UserDO(); user.setId(2L); user.setStatus((byte)0);
        when(users.selectById(2L)).thenReturn(user);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("editor",null,
                List.of(new SimpleGrantedAuthority("user:update"))));
    }
    @AfterEach void cleanup() { SecurityContextHolder.clearContext(); }
    @Test void cannotSmuggleStatusChangeThroughProfileEdit() {
        var command=new UserUpdateDTO(); command.setStatus((byte)1);
        assertThrows(AccessDeniedException.class,()->service.update(2L,command));
        verify(authorization,never()).bumpUser(anyLong());
        verify(users,never()).update(any(UserDO.class),any());
    }
    @Test void protectsSuperAdminPasswordAndProfile() {
        doThrow(new AccessDeniedException("protected target")).when(authorization).guardAccountMutation(2L);
        assertThrows(AccessDeniedException.class,()->service.update(2L,new UserUpdateDTO()));
        var password=new UserPasswordResetDTO(); password.setNewPassword("validPassword123");
        assertThrows(AccessDeniedException.class,()->service.resetPassword(2L,password));
        verify(users,never()).update(any(UserDO.class),any());
    }
    @Test void lastAdministratorCannotBeDeletedOrBanned() {
        doThrow(new BizException(ResultStatus.ROLE_IN_USE)).when(authorization).protectLastSuperAdmin(2L);
        assertThrows(BizException.class,()->service.deleteById(2L));
        assertThrows(BizException.class,()->service.updateStatus(2L,(byte)1));
        verify(users,never()).deleteById(2L);
        verify(authorization,never()).bumpUser(anyLong());
    }
}
