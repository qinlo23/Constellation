# owl 数据库目录

本目录维护 `owl` 数据库的固定基线和增量版本。`modules/` 保留当前已确定的初始化基线，后续不再修改；所有新增表、改表、索引、约束、初始化数据和数据修正统一通过 `version/` 中的新 SQL 脚本维护。具体约束见 [AGENTS.md](AGENTS.md)。

## 当前规范

- 数据库：`owl`，字符集 `utf8mb4`，排序规则 `utf8mb4_0900_ai_ci`。
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

新建库只需执行 `modules/` 中的基线，RBAC 已包含在内，无需执行 `version/` 中的旧升级脚本。每份模块 SQL 都明确 `USE owl`，可在不同客户端连接中依次执行。

使用 MySQL 命令行可以通过统一入口一次执行（从仓库根目录开始）：

```bash
cd backend/database
mysql -h 127.0.0.1 -u root -p < init.sql
```

`init.sql` 使用 MySQL 客户端 `SOURCE` 命令引用以下文件；若使用图形化数据库工具，先运行 `modules/00_schema.sql`，再按以下顺序运行模块文件，不要按文件名字母顺序批量执行。建表基线用于空库，重复执行会因表已存在而报错。

1. `modules/00_schema.sql`
2. `modules/user_db.sql`
3. `modules/user_blog.sql`
4. `modules/user_gallery.sql`
5. `modules/front_db.sql`
6. `modules/ai_db.sql`
7. `modules/price_db.sql`
8. `modules/caishen_db.sql`
9. `modules/user_init_data.sql`（开发环境示例账号、角色关系和权限目录；生产环境应单独准备账号与角色数据）

`user_init_data.sql` 内的权限目录是当前授权查询所需的基础数据，不能因为跳过示例账号而同时遗漏；生产初始化需保留该权限目录及有效的超级管理员角色和账号关系。
`init.sql` 只引用当前有效基线，不执行归档文件或历史升级脚本。

## 模块说明

| 文件 | 当前用途 |
| --- | --- |
| `00_schema.sql` | 创建并选中小写 schema `owl` |
| `user_db.sql` | 用户、角色、地址、权限目录、用户角色关系、角色权限关系 |
| `user_blog.sql` | user 模块拥有的博客文章、设置、教育经历、技能分类与技能项 |
| `user_gallery.sql` | user 模块拥有的个人画廊 |
| `front_db.sql` | admin 模块的首页焦点、特性、Hero 视频与菜单 |
| `ai_db.sql` | AI 会话与消息；表名沿用 `agent_` 前缀 |
| `price_db.sql` | 价格分类、物品、来源、地区、记录与媒体 |
| `caishen_db.sql` | 基金、净值、关注、提醒与总结 |
| `user_init_data.sql` | 开发示例账号、角色、用户角色关系及权限目录 |

## 废弃脚本与核对结果

以下文件已原样移入 `.rubbish/`，不参与初始化：

- `blog_db.sql`：旧 mango 博客结构，已由 `modules/user_blog.sql` 替代；同时执行会重复创建四张表，且旧字段不符合当前后端。
- `log_db.sql`：旧日志落库结构。当前 common-log 仅输出日志，不写入这三张日志表。
- `20261114_user_rbac.sql`：旧库回填与结构转换脚本，最终结构和权限数据已整合进当前基线，不再参与新库初始化。
- `user_permission_seed.sql`：权限目录与 `modules/user_init_data.sql` 中的 SQL 完全相同，避免维护两个相同入口。

`.rubbish/` 沿用本目录 `.gitignore` 的忽略规则，仅保留在本地；历史已提交文件仍可通过 Git 查阅。
详细核对结论见 [数据库与后端一致性核对](AUDIT.md)。

## RBAC 基线

`modules/user_db.sql` 已直接定义 `role_code`、`built_in`、`auth_version`，以及 `user_permission`、`user_account_role`、`user_role_permission` 三张表；`user_account` 不再包含旧的 `role_name` 关联列。
`modules/user_init_data.sql` 初始化内置 `USER` / `SUPER_ADMIN` 角色、示例用户角色关系和 28 项权限目录。新库不需要执行历史角色回填或删除旧列的 ALTER 语句。
旧的 `version/20261114_user_rbac.sql` 已原样归档至 `.rubbish/`；后续变更仍按 `version/` 增量规则管理。授权行为见 [后台权限说明](../RBAC.md)。

## 连接配置

MySQL schema 名为小写 `owl`；仓库内 JDBC 示例、测试连接和代码生成器已同步。主应用通过 Nacos 导入外部连接配置，实际启动前需将其中 JDBC URL 的数据库名也设为 `owl`。应用名称、Maven 坐标及 Nacos namespace 的 `OWL` 是独立标识，保持原值。
本次只整理新库脚本，不自动将已有 `OWL` 数据库改名或迁移数据。
