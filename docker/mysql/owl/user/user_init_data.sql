-- ======================================================
-- OWL 数据库：user 模块初始化数据
-- 模块：user
-- 说明：先执行 user_db.sql 建表，再执行本文件。
--       脚本可重复执行：角色按 role_code 去重，用户按唯一键去重。
-- 默认密码：
--   admin    / admin123
--   merchant / merchant123
--   user     / user123
--   service  / service123
-- ======================================================

-- ------------------------------------------------------
-- 1. 角色表初始化
-- ------------------------------------------------------
INSERT IGNORE INTO user_role (role_code, role_name, built_in, description, enabled)
VALUES ('USER', 'USER', 1, '普通用户', 1),
       ('MERCHANT', 'MERCHANT', 0, '商家', 1),
       ('SUPER_ADMIN', 'ADMIN', 1, '超级管理员', 1),
       ('CUSTOMER_SERVICE', 'CUSTOMER_SERVICE', 0, '客服', 1);

-- ------------------------------------------------------
-- 2. 用户表初始化
-- ------------------------------------------------------
INSERT IGNORE INTO user_account
(user_code, username, password, phone, email, avatar_url, nickname, remark, status, create_time, update_time)
VALUES
('U10001', 'admin', '$2a$10$tLCILQIBySPOoDkway5MXeeMrZL4YVJKDopPjqntSFuYkGeGO5I0y',
 '13800000001', 'admin@example.com', '/DEFAULT_AVATAR.png', '系统管理员', '初始化管理员账号', 0, NOW(), NOW()),
('U10002', 'merchant', '$2a$10$pIaHyCwE44gTfcPDhzephuKZ9/4aWgjrsz.GTBHnYwBrue1RNWBGm',
 '13800000002', 'merchant@example.com', '/DEFAULT_AVATAR.png', '示例商家', '初始化商家账号', 0, NOW(), NOW()),
('U10003', 'user', '$2a$10$Bp09V58hSk58emMeVn4GleoEd5R/w/i8WVchyKuYB48VbN8zVDzMm',
 '13800000003', 'user@example.com', '/DEFAULT_AVATAR.png', '普通用户', '初始化普通用户', 0, NOW(), NOW()),
('U10004', 'service', '$2a$10$ojjjF5.1Bk6hCa1FbEJomOTYF/kGFanRLsNbvSJ1Lz54hXdhgpupi',
 '13800000004', 'service@example.com', '/DEFAULT_AVATAR.png', '客服人员', '初始化客服账号', 0, NOW(), NOW());

INSERT IGNORE INTO user_account_role(user_id,role_id)
SELECT u.id,r.id FROM user_account u JOIN user_role r
ON r.role_code=CASE u.user_code WHEN 'U10001' THEN 'SUPER_ADMIN'
WHEN 'U10002' THEN 'MERCHANT' WHEN 'U10003' THEN 'USER' WHEN 'U10004' THEN 'CUSTOMER_SERVICE' END
WHERE u.user_code IN ('U10001','U10002','U10003','U10004');
INSERT IGNORE INTO user_permission(code, name, module) VALUES
('admin:access', '进入后台', '后台'),
('dashboard:read', '查看控制台', '后台'),
('content:read', '查看内容管理入口', '后台'),
('settings:read', '查看界面设置', '后台'),
('user:read', '查看', '用户管理'),
('user:create', '新增', '用户管理'),
('user:update', '编辑', '用户管理'),
('user:delete', '删除', '用户管理'),
('user:status', '启用/禁用', '用户管理'),
('user:reset-password', '重置密码', '用户管理'),
('spotlight:read', '查看', '焦点项目'),
('spotlight:create', '新增', '焦点项目'),
('spotlight:update', '编辑', '焦点项目'),
('spotlight:delete', '删除', '焦点项目'),
('feature:read', '查看', '特性管理'),
('feature:create', '新增', '特性管理'),
('feature:update', '编辑', '特性管理'),
('feature:delete', '删除', '特性管理'),
('hero-video:read', '查看', 'Hero 视频'),
('hero-video:create', '新增', 'Hero 视频'),
('hero-video:update', '编辑', 'Hero 视频'),
('hero-video:delete', '删除', 'Hero 视频'),
('hero-video:status', '启用/禁用', 'Hero 视频'),
('menu-item:read', '查看', '首页菜单'),
('menu-item:create', '新增', '首页菜单'),
('menu-item:update', '编辑', '首页菜单'),
('menu-item:delete', '删除', '首页菜单'),
('menu-item:status', '启用/禁用', '首页菜单');
