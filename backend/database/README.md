# OWL 数据库目录

本目录维护 OWL 数据库的固定基线和增量版本。`modules/` 保留当前已确定的初始化基线，后续不再修改；所有新增表、改表、索引、约束、初始化数据和数据修正统一通过 `version/` 中的新 SQL 脚本维护。具体约束见 [AGENTS.md](AGENTS.md)。

## 当前规范

- 数据库：`OWL`，字符集 `utf8mb4`，排序规则 `utf8mb4_0900_ai_ci`。
- 表名：小写 snake_case、单数，不使用保留字。
- 字段名：小写 snake_case，Java 属性使用 camelCase。
- 反引号：不用于普通标识符；仅当字段是 MySQL 保留字或包含特殊字符时必须使用，优先通过改名避免。
- 时间字段：统一为 `create_time` / `update_time`。
- 主键：`BIGINT UNSIGNED AUTO_INCREMENT`；UUID 场景使用 `CHAR(36)`。
- 索引：`idx_<table>_<column>`，唯一索引 `uk_<table>_<column>`，外键 `fk_<table>_<column>`。
- `modules/` 中的现有 SQL 为固定基线，后续变更不得回写或合并到基线；已执行的版本脚本也不得改写，修正必须新增版本。
- 详细命名规范见 [数据库表命名规范](../.docs/spec/naming/database-naming-convention.md)。

## 增量版本规则

- 新脚本统一放在 `version/`，命名为 `YYYYMMDD_NN_description.sql`，同一天的序号递增；已有历史脚本保留原名。
- 脚本头部记录用途、适用的数据库状态、前置依赖、执行顺序、是否可重复执行及验证和回退方法。
- 新建库先执行基线，再执行适用于该基线且尚未包含的增量版本；已有库只执行尚未应用的适用版本，按依赖顺序记录执行结果。
- 不会因目录改名自动执行脚本；Docker 基线副本不通过修改来承载后续数据库变更。

## 基线执行顺序

1. `init.sql`
2. `modules/user_db.sql`
3. `modules/log_db.sql`
4. `modules/front_db.sql`
5. `modules/crow_db.sql`
6. `modules/sugarcane_db.sql`
7. `modules/mango_db.sql`
8. `modules/caishen_db.sql`

## 模块说明

- `user`：`user_role`、`user_account`、`user_address`。
- `log`：`user_log`、`biz_log`、`admin_log`。
- `admin`：`admin_spotlight`、`admin_feature`、`admin_hero_video`、`admin_menu_item`。
- `ai`：`agent_conversation`、`agent_message`。
- `price`：`price_category`、`price_item`、`price_source`、`geo_location`、`price_record`、`price_item_media`。
- `mango`：`blog_category`、`blog_tag`、`blog_post`、`blog_post_tag`、`blog_profile`、`blog_education`、`blog_skill_category`、`blog_skill_item`。`blog_post` 依赖 `user_account` 表，示例数据依赖 `user_init_data.sql` 中的 `admin` 用户。
- `caishen`：`caishen_fund`、`caishen_fund_nav`、`caishen_fund_watch`、`caishen_fund_alert`、`caishen_summary`。`caishen_fund_watch.user_id` 关联 `user_account` 表，`caishen_fund_alert.watch_id` 级联删除；本目录暂未提供独立的演示种子脚本。

`pitaya` 模块已废弃，当前业务目录不再包含该模块，不参与构建。

## RBAC 迁移

用户角色已升级为多对多关系，并增加权限目录、角色权限关联和授权版本。RBAC 改造前的旧库使用 `version/20261114_user_rbac.sql`；发布、回退和测试步骤见 [后台权限说明](../RBAC.md)。当前 `modules/user_db.sql` 和 `modules/user_init_data.sql` 已包含 RBAC，按此基线新建的库不应重复执行该历史升级脚本；后续 RBAC 变更另增版本脚本。
