package com.relax.owl.ai;


import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collections;
import java.nio.file.Path;


/**
 * 代码生成
 *
 * @author slnt23
 * @since 2026/4/3
 */


public class CodeGenerator {

    public static void main(String[] args) throws Exception {

        String projectPath = Path.of(CodeGenerator.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                .getParent().getParent().toString();

        FastAutoGenerator.create(
                        "jdbc:mysql://localhost:3306/pitaya?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8",
                        "root",
                        "123456qin"
                )
                .globalConfig(builder -> {
                    builder
                            .author("slnt23")
                            .enableSwagger()
                            .outputDir(projectPath + "/src/main/java")
                            .disableOpenDir()
                            .commentDate("yyyy-MM-dd HH:mm:ss");
                })

                .packageConfig(builder -> {
                    builder.parent("com.relax.owl.ai")
                            .entity("domain.entity")
                            .mapper("mapper")
                            .service("service")
                            .serviceImpl("service.impl")
                            .controller("controller")
                            // XML 输出到 resources/mapper
                            .pathInfo(Collections.singletonMap(
                                    OutputFile.xml,
                                    projectPath + "/src/main/resources/mapper"
                            ));
                })

                .strategyConfig(builder -> {
                    builder
                            .addInclude("agent_conversation", "agent_message")
                            .addTablePrefix("agent_")

                            .entityBuilder()
                            .enableLombok()
                            .enableTableFieldAnnotation()
                            .formatFileName("%sDO")

                            .mapperBuilder()
                                .mapperAnnotation(Mapper.class)
                                .enableBaseColumnList()

                            .serviceBuilder()
                            .formatServiceFileName("%sService")
                            .formatServiceImplFileName("%sServiceImpl")

                            .controllerBuilder()
                            .enableRestStyle()
                            .enableHyphenStyle();
                })

                .templateEngine(new VelocityTemplateEngine())
                .execute();
    }
}
