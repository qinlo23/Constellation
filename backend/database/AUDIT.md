# 数据库与后端一致性核对

核对日期：2026-11-14。范围为 `backend/database` 全部 SQL、`backend/business` 实体与 Mapper、common-log，以及 Docker MySQL 初始化入口与对应副本。本记录基于仓库静态核对，未连接运行中的数据库，未执行 SQL，不代表现有环境已经升级到这些结构。

## 文件处置

| 文件 | 结论与依据 |
| --- | --- |
| `.rubbish/blog_db.sql` | 旧 mango 结构，文件头已标记废弃。与 user_blog 重复定义 blog_post、blog_education、blog_skill_category、blog_skill_item；旧 blog_post 缺少 user_id、cover_url、tags、status、sort_order、deleted，旧技能项使用 name 而非 item_name。仅保留历史参考。 |
| `.rubbish/log_db.sql` | user_log、biz_log、admin_log 无当前实体、Mapper 或日志持久化调用；OperationLogAspect 仅调用日志输出。移出初始化清单。 |
| `.rubbish/user_permission_seed.sql` | INSERT 语句与 user_init_data 的权限目录完全相同。保留 user_init_data 为基线权限数据入口。 |
| `.rubbish/20261114_user_rbac.sql` | 最终 RBAC 结构和权限数据已核对并整合到当前基线，旧库回填脚本已归档，不参与新库初始化。 |

归档不删除实际数据库中的旧表；归档 SQL 保持原样。`.rubbish/` 被本目录忽略规则排除，不会自动加入 Git。

## 当前代码与有效基线

- 当前 28 个带 `@TableName` 的实体映射覆盖 26 张表；逐项核对实体字段（含隐式驼峰转换）均可在有效基线找到对应列。CaishenUserEmailDO 只投影 user_account 的 id、email，属于有意的局部映射。
- user_permission、user_account_role、user_role_permission 虽无独立实体，但 AuthorizationMapper 使用注解 SQL 直接读写，三张表必须保留。
- user_blog 与当前博客实体一致；user_gallery 与当前画廊实体一致。旧博客分类、标签、文章标签关系和 profile 属于归档结构，不能因名称相近替换当前博客设置表。
- price_record.currency 与 price_item_media.media_type 已在基线中定义为 ENUM，后端使用 String 映射；不是缺列。
- AI、admin、price、caishen、user 的显式 Mapper SQL 所引用的业务表、限定列已与当前有效基线核对。字段存在性一致不等于运行行为或数据完整性已通过集成测试。

## 已修正的初始化说明

原 README 错把旧 blog_db 列入基线执行顺序，遗漏 user_blog、user_gallery 和 user_init_data，导致按说明初始化时博客结构不匹配、画廊缺表、角色和权限数据缺失。已改为现有有效脚本的依赖顺序。当前各模块脚本显式选中 `owl` 数据库，`00_schema.sql` 负责建库，`init.sql` 按依赖顺序调用完整基线。

## 尚存的问题与后续处理

| 位置 | 问题 | 建议 |
| --- | --- | --- |
| `../../docker/mysql-owl/owl/admin/front_db.sql` | 副本仅定义 admin_spotlight、admin_feature，缺少后端使用的 admin_hero_video、admin_menu_item。Docker 空库初始化会缺这两张表。 | 另行调整 Docker 初始化来源；已有库通过新增 version 脚本补表，不能假设更新初始化副本能升级现有数据卷。 |
| `../../docker/mysql-owl/owl/init.sh` | 仍执行旧日志建表脚本，且维护独立副本；旧目录代号 crow、sugarcane 本身不影响执行，但与后端文件名不同。 | 后续统一初始化来源，避免两套脚本漂移；本轮不改 Docker。 |
| `modules/user_init_data.sql` | 将开发示例账号与必要的权限目录混在同一文件，跳过示例数据时容易遗漏权限基础数据。 | 后续按明确的初始化方案拆分；本轮保持固定基线内容不变。 |
| `modules/ai_db.sql`、`modules/price_db.sql` | 模块头部注释已随本次基线整理统一为 ai、price。 | 已修正，不影响表名。 |

其余 Docker 对应副本与后端基线相同；caishen 副本仅有空白差异。没有通过新增兼容结构或重写历史版本消除这些差异。

## 验证边界

执行文件校验值比较、重复建表与外键依赖检查、实体列映射核对、Mapper 表和列引用检查、权限目录重复检查及文档链接检查。初次核对仅归档并修正文档；后续按用户要求将新库 schema 统一为 `owl`、补充建库及执行入口，并归档已被基线覆盖的 RBAC 升级脚本。既有表结构未重复添加，未操作运行数据库，也未搭建数据库测试容器。
