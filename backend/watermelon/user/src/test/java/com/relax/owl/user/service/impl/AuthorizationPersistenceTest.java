package com.relax.owl.user.service.impl;

import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import com.relax.owl.user.mapper.*;
import com.relax.owl.user.service.*;
import com.relax.owl.common.exception.BizException;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import javax.sql.DataSource;
import java.util.List;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

/** Runs against an isolated schema prepared by database/tests/test_user_rbac.py. */
@EnabledIfEnvironmentVariable(named = "RBAC_JDBC_URL", matches = ".+")
class AuthorizationPersistenceTest {
    AnnotationConfigApplicationContext context;
    AuthorizationService authorization;
    RoleService roles;
    JdbcTemplate jdbc;
    long superRole;
    long userRole;
    @Configuration
    @EnableTransactionManagement(proxyTargetClass = true)
    @EnableMethodSecurity(proxyTargetClass = true)
    @MapperScan(basePackageClasses = RoleMapper.class)
    static class Config {
        @Bean DataSource dataSource() {
            String url = System.getenv("RBAC_JDBC_URL");
            if (url == null || !url.startsWith("jdbc:mysql://127.0.0.1:")
                    || !url.contains("/constellation_rbac_java?"))
                throw new IllegalArgumentException("Use only the isolated localhost constellation_rbac_java schema");
            return new DriverManagerDataSource(url,"root","rbac-local-test");
        }
        @Bean SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
            var factory = new MybatisSqlSessionFactoryBean(); factory.setDataSource(dataSource);
            return factory.getObject();
        }
        @Bean PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
        @Bean AuthorizationService authorization(AuthorizationMapper mapper, RoleMapper roles) {
            return new AuthorizationService(mapper,roles);
        }
        @Bean RoleService roles(RoleMapper mapper, AuthorizationService authorization) {
            return new RoleServiceImpl(mapper,authorization);
        }
    }
    static void authenticate(boolean superAdmin) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("test",null,
                List.of(new SimpleGrantedAuthority(superAdmin ? "ROLE_SUPER_ADMIN" : "user:update"))));
    }
    @BeforeEach void setup() {
        context = new AnnotationConfigApplicationContext(Config.class);
        authorization=context.getBean(AuthorizationService.class); roles=context.getBean(RoleService.class);
        jdbc=new JdbcTemplate(context.getBean(DataSource.class));
        jdbc.update("DELETE FROM user_account_role"); jdbc.update("DELETE FROM user_account");
        jdbc.update("DELETE FROM user_role_permission"); jdbc.update("DELETE FROM user_role WHERE built_in=0");
        superRole=jdbc.queryForObject("SELECT id FROM user_role WHERE role_code='SUPER_ADMIN'",Long.class);
        userRole=jdbc.queryForObject("SELECT id FROM user_role WHERE role_code='USER'",Long.class);
        jdbc.update("INSERT INTO user_account(id,user_code,username,email) VALUES(1,'test1','test1','test1@example.com'),(2,'test2','test2','test2@example.com')");
        jdbc.update("INSERT INTO user_account_role(user_id,role_id) VALUES(1,?),(2,?)",superRole,userRole);
        authenticate(true);
    }
    @AfterEach void cleanup() { SecurityContextHolder.clearContext(); context.close(); }
    long editor() {
        var command=new com.relax.owl.user.domain.dto.RoleCreateCommand();
        command.setRoleCode("EDITOR"); command.setRoleName("可修改的展示名称");
        return roles.create(command);
    }
    @Test void realMapperLoadsUnionAndVersionsInvalidateAfterGrantDisableDelete() {
        long editor=editor();
        authorization.replaceRoles(2L,List.of(userRole,editor));
        var previous=authorization.load(2L);
        assertEquals(List.of("USER","EDITOR"),previous.roles());
        authorization.replacePermissions(editor,List.of("feature:update"));
        var granted=authorization.load(2L);
        assertTrue(granted.version()>previous.version());
        assertTrue(granted.permissions().containsAll(List.of("admin:access","feature:read","feature:update")));
        roles.updateEnabled(editor,false);
        var disabled=authorization.load(2L);
        assertTrue(disabled.version()>granted.version());
        assertFalse(disabled.permissions().contains("feature:update"));
        roles.deleteById(editor);
        assertTrue(authorization.load(2L).version()>disabled.version());
        assertEquals(List.of("USER"),authorization.load(2L).roles());
    }
    @Test void failedGrantRollsBackAndCannotChangeBuiltInIdentity() {
        long editor=editor();
        authorization.replaceRoles(2L,List.of(editor));
        authorization.replacePermissions(editor,List.of("feature:read"));
        long version=authorization.load(2L).version();
        assertThrows(BizException.class,()->authorization.replacePermissions(editor,List.of("unknown:permission")));
        assertEquals(version,authorization.load(2L).version());
        assertEquals(List.of("admin:access","feature:read"),authorization.load(2L).permissions());
        assertThrows(BizException.class,()->roles.deleteById(superRole));
        authenticate(false);
        assertThrows(AccessDeniedException.class,()->authorization.replaceRoles(2L,List.of(superRole)));
    }
    @Test void concurrentDemotionsLeaveOneActiveSuperAdministrator() throws Exception {
        authorization.replaceRoles(2L,List.of(superRole));
        var barrier=new CyclicBarrier(2);
        try(var executor=Executors.newFixedThreadPool(2)) {
            Callable<Boolean> first=()->demote(1L,barrier);
            Callable<Boolean> second=()->demote(2L,barrier);
            var a=executor.submit(first); var b=executor.submit(second);
            assertNotEquals(a.get(10,TimeUnit.SECONDS),b.get(10,TimeUnit.SECONDS));
        }
        assertEquals(1L,jdbc.queryForObject("SELECT COUNT(*) FROM user_account_role WHERE role_id=?",Long.class,superRole));
    }
    boolean demote(Long id,CyclicBarrier barrier) throws Exception {
        authenticate(true);
        try {
            barrier.await(5,TimeUnit.SECONDS);
            authorization.replaceRoles(id,List.of(userRole));
            return true;
        } catch(BizException expected) { return false; }
        finally { SecurityContextHolder.clearContext(); }
    }
}
