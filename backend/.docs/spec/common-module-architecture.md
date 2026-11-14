# OWL 公共模块架构

公共模块统一位于 `backend/common/`。该目录的 POM 仅聚合构建，不向业务传递整套依赖。根 POM 管理版本，各使用者声明实际依赖。

## 模块职责

| 模块 | 职责 | 允许的项目内依赖 |
| --- | --- | --- |
| common-core | Result、ResultStatus、ResultPage、PageDTO、基础异常、正则工具 | 无 |
| common-exception | MVC、业务、方法级权限、DAO 异常及 /error 统一响应 | common-core |
| common-web | MVC/CORS 配置、旧业务 Jackson 2 序列化配置 | 无 |
| common-security | JWT、用户上下文、Token 撤销接口、安全响应处理器 | common-core |
| common-mybatis | 数据访问配置、分页转换 | common-core |
| common-mail | 通用邮件发送 | 无 |
| common-redis | Redis、缓存、内部 Jackson 3 序列化 | 无 |
| common-rabbitmq | 通用 AMQP JSON 转换，不声明业务队列 | 无 |
| common-minio | MinIO 配置、文件存储 | 无 |
| common-log | 注解、操作消息模型、切面、请求 traceId | 无 |

公共模块不依赖业务模块，不允许除表中以外的公共模块依赖。第三方框架依赖不受此限制。异常响应和安全响应分别配置具名 Jackson 2 writer，避免依赖其他公共模块的 Bean；Web 为旧业务提供默认 writer。

## 启动与项目配置

common-* 只服务于本项目。common-security 直接维护 SecurityFilterChain 和业务 URL 权限规则；common-web 维护项目 OpenAPI 信息、业务分组及文档 Starter，不再设置 application 模块。

start 的 POM 显式组装业务、所需公共模块和 Nacos Config/Discovery，保留启动类、运行配置和 Spring Boot 打包。start 中仅保留 LogOperatorConfig 这一启动适配：将安全上下文提供给日志接口，保持公共模块互不交叉依赖。当前应用不再因操作日志而引入 RabbitMQ。

业务模块按需依赖公共模块，Spring AI、MapStruct、校验等由实际使用者声明。代码生成器、Velocity 为测试用途。

## 分页、异常与角色

- `ResultPage` 只承载分页数据，`MybatisPageConverter.toResultPage(IPage<T>)` 位于 common-mybatis，保持原返回字段及分页值。
- common-exception 的具体异常处理优先于独立的低优先级兜底处理器；过滤器链认证/权限失败仍由 common-security 的处理器负责。现有 HTTP 状态和业务码不随结构迁移改变。
- `RoleDO` 和 `RoleMapper` 归 user。后台角色管理通过 `RoleService` 的命令与结果 DTO 调用，不直接使用实体或 Mapper；登录仍使用同一份角色数据。
- Spring Security URL 规则由 common-security 按原有次序维护，公开 GET、ADMIN 写操作与默认认证要求保持不变。

## 日志

`@OperationLog` 采集操作信息，由 SLF4J 输出。消息模型不再表示 MQ 消息，注解不再提供 persist 属性。保留业务原返回和原异常；日志模块没有安全、数据库或 MQ 依赖。

可选的 `OperatorProvider` 接口属于日志模块；start 的 LogOperatorConfig 用安全上下文提供实现，没有实现时操作者为空。traceId 从请求头读取或生成，响应回传并在请求结束清理线程上下文。

不再新增操作日志数据库记录。后台日志查询占位代码已删除；既有表、历史数据、部署中的队列及交换机不自动删除。当前不增加日志文件滚动策略，仍由部署日志配置决定输出位置。

## 中间件与历史模块

Redis、RabbitMQ、MinIO 独立引入，不通过 common-infra 打包。订单等业务队列及常量由对应业务拥有。Guava 进程内布隆过滤器归 price，不与 Redis 捆绑。MinIO、邮件等模块仍需要对应配置和外部服务，模块独立不代表可脱离运行条件。

mango、pitaya 保持未纳入 reactor 的历史状态，不在本次重构中恢复。无需升级依赖版本或修改数据库结构。

## 验证

```bash
python3 backend/scripts/check_module_boundaries.py
mvn -f backend/pom.xml verify
```

静态检查覆盖 reactor、项目依赖、公共模块边界及源码引用。Maven 需要 JDK 25 和可解析的依赖仓库。完整运行还需要原有 Nacos 配置、MySQL、Redis、MinIO、邮件与 AI 配置。
