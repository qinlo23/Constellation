-- owl 数据库初始化；MySQL 8.4。
-- 新建数据库时首先执行本文件，再按 README 中的依赖顺序执行其他模块脚本。
CREATE DATABASE IF NOT EXISTS owl
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE owl;
