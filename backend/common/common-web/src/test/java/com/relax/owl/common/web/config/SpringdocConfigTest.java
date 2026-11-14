package com.relax.owl.common.web.config;

import org.junit.jupiter.api.Test;
import org.springdoc.core.models.GroupedOpenApi;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpringdocConfigTest {

    @Test
    void groupsScanExistingBusinessControllersIncludingBlog() throws Exception {
        SpringdocConfig config = new SpringdocConfig();
        List<GroupedOpenApi> groups = List.of(config.userApi(), config.adminApi(),
                config.priceApi(), config.aiApi(), config.caishenApi());
        assertEquals(List.of("用户中心-user", "后台管理中心-admin", "价格中心-price",
                "AI中心-ai", "理财中心-caishen"), groups.stream().map(GroupedOpenApi::getGroup).toList());

        Path backend = Path.of(SpringdocConfigTest.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI()).getParent().getParent().getParent().getParent();
        for (GroupedOpenApi group : groups) {
            String packageName = group.getPackagesToScan().getFirst();
            String module = packageName.split("\\.")[3];
            Path controllers = backend.resolve("business/" + module + "/src/main/java/"
                    + packageName.replace('.', '/'));
            assertTrue(Files.isDirectory(controllers), packageName);
        }
        assertEquals(List.of("com.relax.owl.user.controller"), config.userApi().getPackagesToScan());
        assertTrue(Files.isRegularFile(backend.resolve(
                "business/user/src/main/java/com/relax/owl/user/controller/BlogPostController.java")));
    }
}
