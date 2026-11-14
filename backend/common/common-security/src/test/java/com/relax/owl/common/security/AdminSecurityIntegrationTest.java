package com.relax.owl.common.security;

import com.relax.owl.common.security.config.*;
import com.relax.owl.common.security.handler.*;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.*;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminSecurityIntegrationTest {
    AnnotationConfigWebApplicationContext context;
    MockMvc mvc;
    AuthorizationProvider authorization;
    JwtTokenProvider tokens;
    @Configuration
    @EnableWebMvc
    @Import({SecurityConfig.class, AdminAuthorizationConfig.class, SecurityJsonConfig.class,
            RestAuthenticationEntryPoint.class, RestAccessDeniedHandler.class})
    static class Config {
        @Bean AuthorizationProvider authorization() { return mock(AuthorizationProvider.class); }
        @Bean TokenRevocationService revocation() { return mock(TokenRevocationService.class); }
        @Bean JwtTokenProvider tokens() { return new JwtTokenProvider("test-key-for-rbac-integration-32-bytes",60000); }
        @Bean Endpoints endpoints() { return new Endpoints(); }
    }
    @RestController
    static class Endpoints {
        @GetMapping("/api/admin/feature") @PreAuthorize("permitAll()") public String publicList() { return "public"; }
        @GetMapping("/api/admin/feature/page") @PreAuthorize("hasAuthority('feature:read')") public String read() { return "read"; }
        @PostMapping("/api/admin/feature") @PreAuthorize("hasAuthority('feature:create')") public String write() { return "write"; }
        @GetMapping("/api/admin/unclassified") public String undeclared() { return "must deny"; }
        @PutMapping("/api/admin/user-do/2/roles") @PreAuthorize("hasRole('SUPER_ADMIN')") public String grant() { return "grant"; }
    }
    @BeforeEach void setup() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext()); context.register(Config.class); context.refresh();
        authorization=context.getBean(AuthorizationProvider.class); tokens=context.getBean(JwtTokenProvider.class);
        mvc=MockMvcBuilders.webAppContextSetup(context).addFilters(context.getBean(FilterChainProxy.class)).build();
    }
    @AfterEach void cleanup() { context.close(); }
    String token() { return tokens.generateToken(2L,"code","test@example.com","ADMIN",0,3); }
    void authorize(List<String> roles, List<String> permissions) {
        when(authorization.load(2L)).thenReturn(new AuthorizationProvider.Authorization(3,true,roles,permissions));
    }
    @Test void anonymousCanOnlyReadExplicitPublicList() throws Exception {
        mvc.perform(get("/api/admin/feature")).andExpect(status().isOk());
        mvc.perform(get("/api/admin/feature/page")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/admin/feature")).andExpect(status().isUnauthorized());
    }
    @Test void roleClaimAloneNeverGrantsAccess() throws Exception {
        authorize(List.of("USER"),List.of());
        mvc.perform(get("/api/admin/feature/page").header("Authorization","Bearer "+token())).andExpect(status().isForbidden());
    }
    @Test void readOnlyRoleCannotWriteOrGrantAndUnclassifiedEndpointsDeny() throws Exception {
        authorize(List.of("EDITOR"),List.of("admin:access","feature:read"));
        mvc.perform(get("/api/admin/feature/page").header("Authorization","Bearer "+token())).andExpect(status().isOk());
        mvc.perform(post("/api/admin/feature").header("Authorization","Bearer "+token())).andExpect(status().isForbidden());
        mvc.perform(put("/api/admin/user-do/2/roles").header("Authorization","Bearer "+token())).andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/unclassified").header("Authorization","Bearer "+token())).andExpect(status().isForbidden());
    }
    @Test void changedVersionDisabledAccountAndLegacyTokensRequireLogin() throws Exception {
        when(authorization.load(2L)).thenReturn(new AuthorizationProvider.Authorization(4,true,List.of("EDITOR"),List.of("admin:access","feature:read")));
        mvc.perform(get("/api/admin/feature/page").header("Authorization","Bearer "+token())).andExpect(status().isUnauthorized());
        when(authorization.load(2L)).thenReturn(new AuthorizationProvider.Authorization(3,false,List.of("EDITOR"),List.of("admin:access","feature:read")));
        mvc.perform(get("/api/admin/feature/page").header("Authorization","Bearer "+token())).andExpect(status().isUnauthorized());
        authorize(List.of("SUPER_ADMIN"),List.of("admin:access","feature:read"));
        String legacy=io.jsonwebtoken.Jwts.builder().setId("legacy")
                .claim("userId",2L).claim("tv",0L).claim("role","ADMIN")
                .setExpiration(new java.util.Date(System.currentTimeMillis()+60000))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                        "test-key-for-rbac-integration-32-bytes".getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .compact();
        mvc.perform(get("/api/admin/feature/page").header("Authorization","Bearer "+legacy)).andExpect(status().isUnauthorized());
    }
    @Test void superAdministratorMayGrant() throws Exception {
        authorize(List.of("SUPER_ADMIN"),List.of("admin:access"));
        mvc.perform(put("/api/admin/user-do/2/roles").header("Authorization","Bearer "+token())).andExpect(status().isOk());
    }
}
