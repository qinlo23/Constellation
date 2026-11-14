USE owl;

-- ======================================================
-- owl 数据库：user 模块 canonical DDL
-- 模块：user
-- MySQL 版本：8.4
-- 说明：user_role / user_account / user_address 由 user 模块维护，
--       admin 复用 user_role 表。
-- RBAC 已直接纳入基线：角色编码、内置角色、授权版本、权限目录及多对多关系。
-- 初始账号：数据由 user_init_data.sql 写入，库中存 BCrypt 哈希，明文密码对应如下
--   admin    / admin123       -> ADMIN
--   merchant / merchant123    -> MERCHANT
--   user     / user123        -> USER
--   service  / service123     -> CUSTOMER_SERVICE
-- ======================================================

-- ------------------------------------------------------
-- 1. 角色表
-- ------------------------------------------------------
CREATE TABLE user_role
(
    id          BIGINT UNSIGNED AUTO_INCREMENT COMMENT '主键ID',
    role_code   VARCHAR(64) NOT NULL COMMENT '稳定角色编码',
    built_in    TINYINT(1) NOT NULL DEFAULT 0 COMMENT '内置角色',
    role_name   VARCHAR(100) NOT NULL COMMENT '角色展示名称',
    description VARCHAR(255) COMMENT '角色描述',
    enabled     TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否启用：1=启用，0=禁用',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role_role_code (role_code),
    UNIQUE KEY uk_user_role_role_name (role_name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '角色表';

-- ------------------------------------------------------
-- 2. 用户表
-- ------------------------------------------------------
CREATE TABLE user_account
(
    id          BIGINT UNSIGNED AUTO_INCREMENT COMMENT '主键ID',
    user_code   VARCHAR(64)      NOT NULL COMMENT '账号编号',
    username    VARCHAR(50)      NOT NULL COMMENT '用户名',
    password    VARCHAR(100) COMMENT '密码（加密存储）',
    phone       VARCHAR(20) COMMENT '手机号',
    email       VARCHAR(50)      NOT NULL COMMENT '邮箱',
    avatar_url  VARCHAR(2048) COMMENT '头像URL',
    nickname    VARCHAR(50) COMMENT '昵称',
    remark      VARCHAR(255) COMMENT '备注',
    auth_version BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '授权版本，变更后旧会话失效',
    status      TINYINT(1)       NOT NULL DEFAULT 0 COMMENT '状态：0=正常，1=封禁',
    create_time DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (id),
    UNIQUE KEY uk_user_account_user_code (user_code),
    UNIQUE KEY uk_user_account_username (username),
    UNIQUE KEY uk_user_account_phone (phone),
    UNIQUE KEY uk_user_account_email (email)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '用户表';

-- ------------------------------------------------------
-- 3. 用户收货地址表
-- ------------------------------------------------------
CREATE TABLE user_address
(
    id             BIGINT UNSIGNED AUTO_INCREMENT COMMENT '主键ID',
    user_id        BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    receiver_name  VARCHAR(50) COMMENT '收件人姓名',
    receiver_phone VARCHAR(20) COMMENT '收件人电话',
    province       VARCHAR(50) COMMENT '省',
    city           VARCHAR(50) COMMENT '市',
    district       VARCHAR(50) COMMENT '区县',
    detail         VARCHAR(255) COMMENT '详细地址',
    is_default     TINYINT(1)     NOT NULL DEFAULT 0 COMMENT '是否默认地址：1=是，0=否',
    create_time    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    PRIMARY KEY (id),
    KEY idx_user_address_user_id (user_id),
    CONSTRAINT fk_user_address_user_id FOREIGN KEY (user_id) REFERENCES user_account (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '用户收货地址表';

-- RBAC: role and permission relations belong to user.
CREATE TABLE user_permission
(
    id BIGINT UNSIGNED AUTO_INCREMENT,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    module VARCHAR(100) NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_permission_code (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE user_account_role
(
    id BIGINT UNSIGNED AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    role_id BIGINT UNSIGNED NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_account_role_user_role (user_id, role_id),
    KEY idx_user_account_role_role_id (role_id),
    CONSTRAINT fk_user_account_role_user FOREIGN KEY (user_id) REFERENCES user_account(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_account_role_role FOREIGN KEY (role_id) REFERENCES user_role(id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE user_role_permission
(
    id BIGINT UNSIGNED AUTO_INCREMENT,
    role_id BIGINT UNSIGNED NOT NULL,
    permission_id BIGINT UNSIGNED NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role_permission_role_permission (role_id, permission_id),
    KEY idx_user_role_permission_permission_id (permission_id),
    CONSTRAINT fk_user_role_permission_role FOREIGN KEY (role_id) REFERENCES user_role(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_permission_permission FOREIGN KEY (permission_id) REFERENCES user_permission(id) ON DELETE RESTRICT
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
