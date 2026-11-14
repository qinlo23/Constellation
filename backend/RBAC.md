# 用户、角色与后台权限

角色和权限归 `user` 模块维护，`administration` 只暴露管理接口。`common-security` 通过 `AuthorizationProvider` 加载当前权限，不依赖业务模块。首批覆盖后台用户、角色及首页展示管理；内容管理和控制台保留原占位能力。

## 授权规则

- 用户可关联多个角色，启用角色的权限取并集。角色编码不可修改，展示名称可以修改。禁用或删除角色会撤销其授权，但正常账号仍可重新登录；封禁账号才会阻止登录。
- `SUPER_ADMIN` 是内置超级管理员，自动获得代码维护的全部权限；只有该角色可管理角色、配置权限、分配用户角色。`USER` 是内置普通用户，继续使用既有个人业务能力。
- 内置角色不可删除、禁用或重新配置权限。自定义角色默认没有后台权限，授权时按业务勾选。新增操作权限自动包含同业务的查看权限和 `admin:access`。
- 页面、菜单和按钮使用同一权限编码，实际操作由后端 `@PreAuthorize` 校验。后台新接口未声明方法或类级授权策略时默认拒绝。公开读取仅豁免焦点/特性列表的准确 GET 路径。
- 用户编辑接口不修改角色。非超级管理员不能修改超级管理员的资料、凭据或状态。编辑用户状态还需要 `user:status`，防止借资料编辑接口绕过状态权限。Hero 视频和首页菜单的通用编辑接口同样不能绕过各自的状态权限。
- 在数据库事务内锁定内置超级管理员角色行，串行处理授权和管理账号变更，保证最后一个有效超级管理员不会被并发降级、删除或封禁。
- 授权变更在同事务中更新受影响用户的 `auth_version`。请求按一致数据库快照校验 JWT 的 `av` 并加载角色权限；版本不匹配、账号封禁或账号删除都使受保护请求返回 401。Redis 的既有 Token 版本和登出黑名单继续生效。
- 角色分配、权限配置及角色生命周期的前后值在事务提交后记入结构化应用日志；失败结果沿用操作日志。首版不缓存后端授权结果、不提供部门数据范围或用户直接授权。

## 接口变更

| 接口 | 行为 |
| --- | --- |
| `GET /api/user/info` | 增加 `roles: string[]`、`permissions: string[]`；旧 `role` 仅用于展示 |
| `GET /api/admin/user-do?roleId=...` | 按角色 ID 筛选，用户列表返回 `roles` 数组 |
| `POST /api/admin/user-do` | 可选 `roleIds: number[]`；仅超级管理员可指定，省略则分配 USER |
| `PUT /api/admin/user-do/{id}` | 只编辑资料和有权修改的状态，不接受角色分配 |
| `PUT /api/admin/user-do/{id}/roles` | `{ "roleIds": [1, 2] }`，整体替换；不允许空列表 |
| `POST /api/admin/role-do` | 增加必填 `roleCode`，2–64 位大写字母、数字、下划线，以字母开头 |
| `GET /api/admin/role-do/permissions` | 代码维护的权限目录，按 module 分组 |
| `GET /api/admin/role-do/{id}/permissions` | 已分配的权限编码列表 |
| `PUT /api/admin/role-do/{id}/permissions` | `{ "permissions": ["feature:read"] }`，整体替换，空列表清空授权 |

旧的 `/user-do/{id}/role` 已移除。角色返回值增加 `roleCode` 和 `builtIn`。当前基线的权限目录见 `database/modules/user_permission_seed.sql`，其中 28 项与当前初始化脚本、历史增量迁移一致；后续权限目录变更统一新增到 `database/version/`，不修改基线或历史版本；角色管理入口由 SUPER_ADMIN 身份决定，不开放可委派的角色管理权限。

## 发布与回退

1. 进入维护窗口，停止旧版本写入，备份整个 OWL 数据库并保留旧版本应用制品。
2. 对 RBAC 改造前的旧库执行 `database/version/20261114_user_rbac.sql`，使用 MySQL 客户端批处理，**不要使用 `--force`**。预检会输出孤立角色账号并中止；还要求存在有效 ADMIN 和启用的 USER 角色。
3. 脚本将 ADMIN 映射为 SUPER_ADMIN、USER 保留 USER，其他角色生成 `CUSTOM_<旧ID>`，保留展示名称；回填并核对账户角色数量后移除旧账户 `role_name`。自定义角色不自动获得权限。
4. 同时部署新后端与前端。所有旧登录态都需要重新登录，随后由超级管理员配置自定义角色权限。
5. 检查管理员登录、角色分配、只读角色、公开列表和普通用户个人业务；观察结构化授权日志及 401/403。

MySQL DDL 会自动提交。本脚本仅执行一次，不能通过外层事务整体回滚；出现中途错误或需要回退时，保持维护状态，恢复数据库备份与旧应用，不能在部分迁移库上直接重跑脚本。新建库执行 `database/modules/` 中固定的 `user_db.sql` 和 `user_init_data.sql`，再执行适用于该基线的后续增量版本。当前基线已包含 RBAC，不重复执行 `20261114_user_rbac.sql`；Docker 初始化副本保持基线用途。数据库维护规则见 `database/AGENTS.md`。

## 验证

需要 Java 25、Node 和 MySQL 8.4。

```sh
cd backend
mvn -pl watermelon/administration,common/common-exception -am test
```

```sh
cd frontend
npm ci
npm run build
npx playwright install chromium
npm run preview -- --host 127.0.0.1 --port 5179 --strictPort
# 另一个终端运行；接口全部模拟，不修改业务数据：
npm run test:permissions
```

浏览器测试覆盖匿名访问、伪造本地权限、菜单与按钮、首个可访问页面、刷新、401 退出、多角色分配、权限配置、内置角色保护及管理员访问前台。服务端测试独立验证真实鉴权边界。
