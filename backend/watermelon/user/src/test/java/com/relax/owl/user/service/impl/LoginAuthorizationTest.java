package com.relax.owl.user.service.impl;

import com.relax.owl.user.domain.dto.PasswordLoginDTO;
import com.relax.owl.user.domain.entity.UserDO;
import com.relax.owl.user.mapper.*;
import com.relax.owl.user.utils.CodeCacheUtil;
import com.relax.owl.common.mail.MailService;
import com.relax.owl.common.exception.LoginFailureException;
import com.relax.owl.common.security.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginAuthorizationTest {
    @Test void enabledAccountCanLoginAfterAllItsRolesWereRemoved() {
        var users=mock(UserMapper.class);
        var encoder=mock(PasswordEncoder.class);
        var authorization=mock(AuthorizationMapper.class);
        var tokens=new JwtTokenProvider("test-key-for-login-rbac-test-32-bytes",60000);
        var service=new LoginServiceImpl(mock(MailService.class),users,mock(StringRedisTemplate.class),encoder,
                mock(CodeCacheUtil.class),mock(RoleMapper.class),authorization,tokens,mock(TokenRevocationService.class));
        var user=new UserDO(); user.setId(2L); user.setUserCode("test"); user.setEmail("test@example.com");
        user.setStatus(0); user.setPassword("hash"); user.setAuthVersion(9L);
        when(users.selectOne(any())).thenReturn(user);
        when(encoder.matches("password123","hash")).thenReturn(true);
        when(authorization.roles(2L)).thenReturn(List.of());
        var request=new PasswordLoginDTO(); request.setEmail("test@example.com"); request.setPassword("password123");
        var claims=tokens.parseToken(service.login(request));
        assertEquals(9L,claims.get(JwtConstants.CLAIM_AUTH_VERSION,Long.class));
        assertNull(claims.get(JwtConstants.CLAIM_ROLE));
        user.setStatus(1);
        assertThrows(LoginFailureException.class,()->service.login(request));
    }
}
