# CLAUDE.md

本文件为 Claude Code（claude.ai/code）在此仓库中工作时提供指导。

## 构建 / 运行命令

```bash
# 构建所有模块（maven-surefire-plugin 配置中默认跳过测试）
mvn clean install -f smart-workshop-parent/pom.xml

# 运行测试（覆盖 skipTests 配置）
mvn test -f smart-workshop-parent/pom.xml -DskipTests=false

# 运行单个测试类
mvn test -f smart-workshop-server/pom.xml -DskipTests=false -Dtest=SmartWorkshopApplicationTests

# 将 server 打包为可运行的 Spring Boot jar
mvn clean package -f smart-workshop-parent/pom.xml -DskipTests

# 运行 Spring Boot 应用
mvn spring-boot:run -f smart-workshop-server/pom.xml
```

## 模块架构

这是一个**多模块 Maven 项目**（Java 21，Spring Boot 3.5.7），用于智能制造 MES（制造执行系统）。父 POM 位于 `smart-workshop-parent/pom.xml`，统一管理所有依赖版本，并聚合四个子模块：

| 模块                        | 职责                            | 主要内容                                                                                                            |
| --------------------------- | ------------------------------- | ------------------------------------------------------------------------------------------------------------------- |
| `smart-workshop-pojo`       | 领域对象层（无数据库/Web 依赖） | `entity/`、`dto/`、`vo/`、`enums/`                                                                                  |
| `smart-workshop-common`     | 共享基础设施                    | `JwtUtils`、`CorsConfig`、`globalExceptionHandler`、`BusinessException`、`JsonToMapTypeHandler`                     |
| `smart-workshop-production` | 核心生产领域逻辑                | 状态机（`orderStateMachine`、`planStateMachine`、`workOrderStateMachine`）、`StateContext`、`PermissionPolicy` 接口 |
| `smart-workshop-server`     | 应用入口、Web 层                | `SmartWorkshopApplication`、控制器、服务、映射器、XML、过滤器、拦截器                                               |

**依赖链**：`server` → `production` → `common` → `pojo`（server 依赖所有模块；pojo 不依赖任何模块）。

## 请求处理流程

每个 HTTP 请求依次经过：

1. **`LoginFilter`**（`@WebFilter "/*"`）—— Servlet 过滤器。放行 `/login` 请求。对于其他所有路径，从 `Authorization: Bearer xxx` 或 `token` 请求头中提取 JWT，使用 `JwtUtils.parseToken()` 进行验证。验证失败返回 401。
2. **`PermissionInterceptor`**（`WebConfig` 注册到 `/**`）—— Spring 拦截器。检查处理器方法或其声明类上是否有 `@RequirePermission` 注解。如果存在，解析 token 获取 userId，调用 `AuthService.getUserPermissionCodes()`，根据 AND/OR 逻辑校验所需权限。校验失败返回 403。
3. **Controller** → **Service** → **Mapper**（MyBatis XML）。

**排除的路径**（无需权限校验）：`/login`、`/error`、`/permission/**`、`/role/**`、`/auth/**`。

## RBAC 权限系统

最近添加的基于角色的访问控制系统包含以下组件：

- **`@RequirePermission`** 注解 —— 声明在控制器方法或类上。接收 `value`（权限编码字符串数组）和 `logical`（`AND`/`OR`）。
- **`AuthService`** / **`AuthServiceImpl`** —— 获取用户角色和权限。`getUserPermissionCodes(userId)` 返回用户的所有权限编码（通过 `PermissionMapper.getUserPermissionCodes()` 实现，该查询连接了 user → user_role → role_permission → permission）。
- **`PermissionService`**、**`RoleService`**、**`UserRoleService`**、**`RolePermissionService`** —— RBAC 实体的增删改查服务。
- **实体**：`User`、`Role`、`Permission`、`UserRole`（用户↔角色关联）、`RolePermission`（角色↔权限关联）。

为新增接口添加权限控制：在控制器方法上添加 `@RequirePermission({"权限编码"})` 即可。

## 状态机系统

`smart-workshop-production` 模块包含生产领域的状态机逻辑：

- **`StateEnum`** —— 五种状态：`CREATED → RELEASED → RUNNING → PAUSED/COMPLETED`。`next(ActionEnum)` 方法编码了所有合法的状态转换。
- **`ActionEnum`** —— 六种动作：`PUBLISH`、`CANCEL_PUBLISH`、`PAUSE`、`RESUME`、`START_WORK`、`FINISH_WORK`。
- **状态机**（`orderStateMachine`、`planStateMachine`、`workOrderStateMachine`）—— 每个都是 `@Component`，内部使用 `EnumMap<StateEnum, Set<ActionEnum>>` 定义每个状态下允许的动作。`check(StateEnum, ActionEnum)` 方法对非法转换抛出 `IllegalStateException`。这些是纯规则校验器 —— 不访问数据库，不涉及权限。
- **`StateContext`** —— 值对象，持有 `bizNo`、`action`、`userId`，在服务层、策略层和审计层之间传递。
- **`PermissionPolicy`** 接口及其实现（如 `planPermissionPolicy`）—— 与状态机分离。校验谁可以执行哪个动作（例如，只有"生产主管"可以发布/暂停/恢复计划）。校验失败抛出 `SecurityException`。
- **状态服务模式**（如 `planStateServiceImpl.handle()`）：权限校验 → 状态机校验 → 业务验证 → 状态持久化 → 审计记录。

## 控制器：状态-动作模式

对于具有状态的领域对象（Plan、Order、WorkOrder），控制器遵循 RESTful 的动作分发模式：

```
POST /{resource}/{resourceNo}/actions/{action}
```

其中 `action` 是 URL 路径中的 `ActionEnum` 值，Spring 会自动转换路径变量。控制器捕获特定异常类型（`BusinessException` → 400，`SecurityException` → 403，`IllegalStateException` → 400），并映射为 `Result` 错误响应。

## 关键约定

- **包路径**：所有代码位于 `com.xtax` 下。
- **响应格式**：所有控制器返回 `com.xtax.vo.Result`（封装 `code`、`message`、`data`）。使用 `Result.success(data)` / `Result.error(msg)`。
- **分页**：`com.xtax.vo.ResultPage<T>`（封装 `total` + `rows`）。PageHelper 可用于 MyBatis 分页。
- **日志**：通过 Lombok `@Slf4j` 使用 SLF4J。Logback 配置位于 `logback.xml`（控制台 + 滚动文件）。
- **MyBatis**：映射器接口位于 `com.xtax.mapper`，XML 文件位于 `src/main/resources/com/xtax/mapper/`。全局启用 `map-underscore-to-camel-case: true`。
- **JWT**：claims 存储为 `Map<String, Object>`，至少包含 `"id"`（userId，Integer 类型）。Token 有效期 24 小时。密钥硬编码在 `JwtUtils` 中。
- **Lombok**：广泛使用 `@Data`、`@Slf4j`、`@NoArgsConstructor`、`@AllArgsConstructor`。
- **命名规范**：混合风格 —— 较新的类使用 PascalCase（`AuthService`、`PermissionInterceptor`），较旧的类使用 camelCase（`userService`、`bomController`）。扩展现有模块时遵循该模块的既有风格。
- **测试默认跳过**：`maven-surefire-plugin` 配置中 `<skipTests>true</skipTests>`。
- **设计文档**位于 `docx/` 目录（已被 gitignore）：数据库设计、API 文档、需求规格说明书、RBAC 设计、状态/权限总结。

## 注意事项

- **输出语言**：中文
- **代码注释**：生成的代码必须包含中文注释（类、方法、字段等）
