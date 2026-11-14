package com.relax.owl.common.web.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Knife4j 文档配置。
 * 本项目专用 Web 配置，组装项目文档信息与各业务 API 分组。
 *
 * @author slnt23
 * @since 2025/11/21
 */
@Configuration
public class SpringdocConfig {

    final String securitySchemeName = "JwtAuth";

    /**
     * 全局 OpenAPI 信息：标题、描述、版本、JWT 安全方案。
     */
    @Bean
    public OpenAPI openAllAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("OWL 个人兴趣平台后端")
                        .description("集成用户认证/个人中心、AI 对话、价格追踪、博客与后台管理模块的接口文档。")
                        .version("v0.0.1（开发版）")
                        .contact(new Contact()
                                .name("OWL 开发团队")
                        )
                )
                // 全局 JWT 安全方案，所有 API 分组共用
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .name("Authorization")
                                        .description("登录接口返回 token 后，在请求头 Authorization 中携带：Bearer <token>")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
    }

    /**
     * 用户中心 API 分组：认证、验证码、个人资料、收货地址、博客与画廊。
     */
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("用户中心-user")
                .packagesToScan("com.relax.owl.user.controller")
                .build();
    }

    /**
     * 后台管理中心 API 分组：用户、角色、内容配置。
     */
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("后台管理中心-admin")
                .packagesToScan("com.relax.owl.admin.controller")
                .build();
    }

    /**
     * 价格中心 API 分组：价格追踪、比价、来源与地区对比。
     */
    @Bean
    public GroupedOpenApi priceApi() {
        return GroupedOpenApi.builder()
                .group("价格中心-price")
                .packagesToScan("com.relax.owl.price.controller")
                .build();
    }

    /**
     * AI 中心 API 分组：对话、会话管理。
     */
    @Bean
    public GroupedOpenApi aiApi() {
        return GroupedOpenApi.builder()
                .group("AI中心-ai")
                .packagesToScan("com.relax.owl.ai.controller")
                .build();
    }

    /**
     * 理财中心 API 分组：基金档案、净值、关注、提醒规则、总结。
     */
    @Bean
    public GroupedOpenApi caishenApi() {
        return GroupedOpenApi.builder()
                .group("理财中心-caishen")
                .packagesToScan("com.relax.owl.caishen.controller")
                .build();
    }

}
