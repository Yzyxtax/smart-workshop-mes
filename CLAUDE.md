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

- **`StateEnum`** —— 六种状态：`CREATED → RELEASED → RUNNING → PAUSED/COMPLETED/TERMINATED`。`next(ActionEnum)` 方法编码了所有合法的状态转换。提供工具方法 `isTerminal()`（COMPLETED/TERMINATED）和 `isPreExecution()`（CREATED/RELEASED），用于级联操作中的状态判定。
- **`ActionEnum`** —— 七种动作：`PUBLISH`、`CANCEL_PUBLISH`、`PAUSE`、`RESUME`、`START_WORK`、`FINISH_WORK`、`TERMINATE`。
- **状态机**（`orderStateMachine`、`planStateMachine`、`workOrderStateMachine`）—— 每个都是 `@Component`，内部使用 `EnumMap<StateEnum, Set<ActionEnum>>` 定义每个状态下允许的动作。`check(StateEnum, ActionEnum)` 方法对非法转换抛出 `IllegalStateException`。这些是纯规则校验器 —— 不访问数据库，不涉及权限。
- **`StateContext`** —— 值对象，持有 `bizNo`、`action`、`userId`，在服务层、策略层和审计层之间传递。
- **`PermissionPolicy`** 接口及其实现（`planPermissionPolicy`、`orderPermissionPolicy`、`workOrderPermissionPolicy`）—— 与状态机分离。校验谁可以执行哪个动作（例如，只有"生产主管"可以发布/暂停/恢复计划）。校验失败抛出 `SecurityException`。
- **`GatePolicyInterface`** 及其实现 `GatePolicy` —— 关键动作（PUBLISH）前的业务门禁校验。按 Capacity Gate（产线可用性）→ Process Gate（工艺完整性）→ Skill Gate（人员技能）三级顺序检查。校验失败抛出 `BusinessException`。
- **`AuditService`** —— 所有状态变更均通过 `auditMapper.addAudit()` 写入 `status_history` 表，记录 `targetType`、`targetNo`、`oldStatus`、`newStatus`、`operatorId`。
- **状态服务管道**（`planStateServiceImpl`、`orderStateServiceImpl`、`workOrderStateServiceImpl`，全部位于 `service/serviceImpl/` 下）—— 每个 `handle()` 方法遵循完全一致的 8 步管道：
  1. 获取业务对象并构建 `StateContext`
  2. **权限校验**（PermissionPolicy）
  3. **状态机校验**（StateMachine）—— 纯规则，不查数据库
  4. **门禁校验**（GatePolicy）—— 仅在 PUBLISH 等关键决策动作时触发
  5. **业务验证** —— TERMINATE 前检查关联对象状态等
  6. **状态持久化** —— 使用 `fromStatus.next(action)` 计算新状态，按动作类型差异化更新（如 START_WORK 同时锚定实际开始时间）
  7. **审计记录** —— 写入 status_history
  8. **级联联动** —— 向下创建/变更子对象，或向上同步父对象状态

## 三层级联状态模型

系统的核心架构是 **Plan → Order → WorkOrder** 三级生产层次，状态变更通过级联联动在层级间传播：

```
Plan（生产计划）
├── PUBLISH → 根据工艺流程和可用产线拆分为 Order
├── PAUSE/RESUME → 级联暂停/恢复所有关联 Order
├── CANCEL_PUBLISH/TERMINATE → 级联作废所有非终态 Order
│
Order（生产订单）
├── PUBLISH → 为产线各工序生成 WorkOrder 并自动发布
├── PAUSE/RESUME → 级联暂停/恢复所有关联的 RUNNING/PAUSED WorkOrder
├── CANCEL_PUBLISH/TERMINATE → 级联作废所有非终态 WorkOrder
│
WorkOrder（工单，最靠近现场的执行层）
├── START_WORK/FINISH_WORK → 由员工手动确认（需要 userId 归属校验）
├── PAUSE/RESUME → 员工（需 userId 匹配）或主管（跨工单）操作
└── TERMINATE → 仅 RELEASED/PAUSED 状态可作废
```

**向下联动**（父 → 子）：父级状态变更自动触发子级操作。Plan PUBLISH 自动创建 Order；Order PUBLISH 自动创建并发布 WorkOrder。

**向上同步**（子 → 父）：关键工单（`is_critical=true`）状态变更触发订单状态聚合，订单状态变更再触发计划状态聚合：
- `workOrderStateServiceImpl.handleLinkage()` → 聚合 Order 下所有关键工单 → 判定并更新 Order 状态
- `orderStateServiceImpl.handleLinkage()` → 调用 `planStateServiceImpl.syncPlanStatusFromOrders()` → 单次遍历聚合所有 Order（O(N)），按 **RUNNING > COMPLETED > PAUSED** 优先级判定 Plan 新状态

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
