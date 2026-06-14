# CLAUDE.md

本文件为 Claude Code（claude.ai/code）在此仓库中工作时提供指导。

## 构建 / 运行命令

```bash
# 构建所有模块（测试已启用，不再跳过）
mvn clean install -f smart-workshop-parent/pom.xml

# 仅编译跳过测试
mvn clean install -f smart-workshop-parent/pom.xml -DskipTests

# 运行单个测试类
mvn test -f smart-workshop-server/pom.xml -Dtest=SmartWorkshopApplicationTests

# AI 模块测试
mvn test -f smart-workshop-ai/pom.xml

# 将 server 打包为可运行的 Spring Boot jar
mvn clean package -f smart-workshop-parent/pom.xml -DskipTests

# 运行 Spring Boot 应用（需先设置 AI_API_KEY 环境变量）
mvn spring-boot:run -f smart-workshop-server/pom.xml
```

**注意**：项目没有 Maven Wrapper（`mvnw`），需自行安装 Maven。父 POM 是 `smart-workshop-parent/pom.xml`，所有 `mvn` 命令需通过 `-f` 指向它。

## 模块架构

**多模块 Maven 项目**（Java 21，Spring Boot 3.5.7），智能制造 MES 系统。父 POM 统一管理依赖版本，聚合五个子模块：

| 模块                        | 职责                       | 主要内容                                                                                                            |
| --------------------------- | -------------------------- | ------------------------------------------------------------------------------------------------------------------- |
| `smart-workshop-pojo`       | 领域对象层（无外部依赖）   | `entity/`、`dto/`、`vo/`、`enums/`                                                                                  |
| `smart-workshop-common`     | 共享基础设施               | `JwtUtils`、`CorsConfig`、`globalExceptionHandler`、`BusinessException`、`JsonToMapTypeHandler`                     |
| `smart-workshop-production` | 核心生产领域逻辑           | 状态机（`orderStateMachine`、`planStateMachine`、`workOrderStateMachine`）、`StateContext`、`PermissionPolicy` 接口 |
| `smart-workshop-ai`         | AI 智能交互模块            | `@AiTool` 工具系统、LLM Provider（Claude/OpenAI）、7 步沙箱执行管道、SSE 流式对话、审计日志                       |
| `smart-workshop-server`     | 应用入口、Web 层、工具实现 | `SmartWorkshopApplication`、控制器、服务、映射器、XML、过滤器、拦截器、15 个 AI Tool 实现                           |

**依赖链**：`server` → `ai` → `production` → `common` → `pojo`

### AI 模块架构（smart-workshop-ai）

AI 模块实现**自然语言操作 MES 系统**的多轮工具调用循环：

```
用户消息 → AiChatController(SSE) → AiAgentService
  → LLM Provider(Claude/OpenAI, 通过 WebClient 流式调用)
  → 解析 Tool Call → ToolExecutor(7步沙箱管道)
  → 业务 Tool 执行 → 结果返回 LLM → ...循环最多5轮...
  → 最终文本回复
```

- **`@AiTool` / `@ToolParam` 注解**：业务工具通过注解声明，启动时自动扫描注册并生成 JSON Schema
- **7 步沙箱执行管道**（`ToolExecutor`）：工具查找 → 参数校验 → RBAC 权限 → 确认检查 → 速率限制 → 业务执行 → 审计记录
- **双 Provider 支持**：`ClaudeProvider` / `OpenAiProvider`，通过 `application-ai.yml` 切换，均通过 WebClient 直接调用，**不依赖第三方 LLM SDK**
- **SSE 事件类型**：`THINKING`、`TEXT_DELTA`、`TOOL_CALL`、`TOOL_RESULT`、`DONE`、`ERROR`
- **Tool 实现**位于 `smart-workshop-server/src/main/java/com/xtax/ai/tool/`，分 7 类（BOM、设备、产线、工艺、工序、班组、生产），共 15 个工具
- **权限桥接**（`AiPermissionBridge`）：将 `AuthService.getUserPermissionCodes()` 注入 `ToolExecutor` 作为权限检查函数

**AI 模块配置**（`smart-workshop-ai/src/main/resources/application-ai.yml`，由 `application.yml` 通过 `spring.config.import` 导入）：
- 默认 Provider：`claude`，Model：`claude-opus-4-8`
- API Key：通过环境变量 `${AI_API_KEY}` 注入（必填）
- 最大工具调用轮数：5，历史轮数：20，会话保留：90 天
- 速率限制：每会话每分钟 10 次工具调用

## 请求处理流程

每个 HTTP 请求依次经过：

1. **`LoginFilter`**（`@WebFilter "/*"`）—— Servlet 过滤器。放行 `/login` 请求。对于其他所有路径，从 `Authorization: Bearer xxx` 或 `token` 请求头中提取 JWT，使用 `JwtUtils.parseToken()` 进行验证。验证失败返回 401。
2. **`PermissionInterceptor`**（`WebConfig` 注册到 `/**`）—— Spring 拦截器。检查处理器方法或其声明类上是否有 `@RequirePermission` 注解。如果存在，解析 token 获取 userId，调用 `AuthService.getUserPermissionCodes()`，根据 AND/OR 逻辑校验所需权限。校验失败返回 403。
3. **Controller** → **Service** → **Mapper**（MyBatis XML）。

**排除的路径**（无需权限校验）：`/login`、`/error`、`/permission/**`、`/role/**`、`/auth/**`、`/ai/**`。

## RBAC 权限系统

基于角色的访问控制：

- **`@RequirePermission`** 注解 —— 声明在控制器方法或类上。接收 `value`（权限编码字符串数组）和 `logical`（`AND`/`OR`）。
- **`AuthService`** / **`AuthServiceImpl`** —— `getUserPermissionCodes(userId)` 返回用户的所有权限编码（查询走 user → user_role → role_permission → permission 五表联查）。
- **`PermissionService`**、**`RoleService`**、**`UserRoleService`**、**`RolePermissionService`** —— RBAC 实体的增删改查服务。
- **实体**：`User`、`Role`、`Permission`、`UserRole`（用户↔角色关联）、`RolePermission`（角色↔权限关联）。

为新增接口添加权限控制：在控制器方法上添加 `@RequirePermission({"权限编码"})` 即可。

## 状态机系统

`smart-workshop-production` 模块包含生产领域的状态机逻辑：

- **`StateEnum`** —— 六种状态：`CREATED → RELEASED → RUNNING → PAUSED/COMPLETED/TERMINATED`。`next(ActionEnum)` 编码所有合法状态转换。`isTerminal()`（COMPLETED/TERMINATED）和 `isPreExecution()`（CREATED/RELEASED）用于级联操作判定。
- **`ActionEnum`** —— 七种动作：`PUBLISH`、`CANCEL_PUBLISH`、`PAUSE`、`RESUME`、`START_WORK`、`FINISH_WORK`、`TERMINATE`。
- **状态机**（`orderStateMachine`、`planStateMachine`、`workOrderStateMachine`）—— 每个都是 `@Component`，内部使用 `EnumMap<StateEnum, Set<ActionEnum>>` 定义每个状态下允许的动作。`check(StateEnum, ActionEnum)` 对非法转换抛出 `IllegalStateException`。纯规则校验器，不查数据库，不涉及权限。
- **`StateContext`** —— 值对象，持有 `bizNo`、`action`、`userId`，在服务层、策略层和审计层之间传递。
- **`PermissionPolicy`** 接口及其实现 —— 与状态机分离，校验谁可以执行哪个动作（例如只有"生产主管"可以发布/暂停/恢复计划）。校验失败抛出 `SecurityException`。
- **`GatePolicyInterface`** 及其实现 `GatePolicy` —— 关键动作（PUBLISH）前的业务门禁校验。按 Capacity Gate（产线可用性）→ Process Gate（工艺完整性）→ Skill Gate（人员技能）三级顺序检查。失败抛 `BusinessException`。
- **`AuditService`** —— 所有状态变更通过 `auditMapper.addAudit()` 写入 `status_history` 表，记录 `targetType`、`targetNo`、`oldStatus`、`newStatus`、`operatorId`。
- **状态服务管道**（`planStateServiceImpl`、`orderStateServiceImpl`、`workOrderStateServiceImpl`）—— 每个 `handle()` 方法遵循 8 步管道：
  1. 获取业务对象并构建 `StateContext`
  2. **权限校验**（PermissionPolicy）
  3. **状态机校验**（StateMachine）
  4. **门禁校验**（GatePolicy）—— 仅在 PUBLISH 等关键决策动作时触发
  5. **业务验证** —— TERMINATE 前检查关联对象状态
  6. **状态持久化** —— 使用 `fromStatus.next(action)` 计算新状态，按动作类型差异化更新
  7. **审计记录** —— 写入 status_history
  8. **级联联动** —— 向下创建/变更子对象，或向上同步父对象状态

## 三层级联状态模型

**Plan → Order → WorkOrder** 三级生产层次，状态变更通过级联联动传播：

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

**向上同步**（子 → 父）：关键工单（`is_critical=true`）状态变更触发订单状态聚合，订单状态变更触发计划状态聚合：
- `workOrderStateServiceImpl.handleLinkage()` → 聚合 Order 下所有关键工单 → 判定并更新 Order 状态
- `orderStateServiceImpl.handleLinkage()` → 调用 `planStateServiceImpl.syncPlanStatusFromOrders()` → 单次遍历聚合所有 Order（O(N)），按 **RUNNING > COMPLETED > PAUSED** 优先级判定 Plan 新状态

## 控制器：状态-动作模式

具有状态的领域对象（Plan、Order、WorkOrder）遵循 RESTful 动作分发模式：

```
POST /{resource}/{resourceNo}/actions/{action}
```

`action` 是 URL 路径中的 `ActionEnum` 值，Spring 自动转换路径变量。控制器捕获特定异常（`BusinessException` → 400，`SecurityException` → 403，`IllegalStateException` → 400），映射为 `Result` 错误响应。

## 关键约定

- **包路径**：所有代码位于 `com.xtax` 下。
- **响应格式**：所有控制器返回 `com.xtax.vo.Result`（封装 `code`、`message`、`data`）。使用 `Result.success(data)` / `Result.error(msg)`。
- **分页**：`com.xtax.vo.ResultPage<T>`（封装 `total` + `rows`）。PageHelper 用于 MyBatis 分页。
- **日志**：通过 Lombok `@Slf4j` 使用 SLF4J。Logback 配置位于 `logback.xml`（控制台 + 滚动文件）。
- **MyBatis**：映射器接口位于 `com.xtax.mapper`，XML 文件位于 `src/main/resources/com/xtax/mapper/`。全局启用 `map-underscore-to-camel-case: true`。
- **JWT**：claims 存储为 `Map<String, Object>`，至少包含 `"id"`（userId，Integer 类型）。Token 有效期 24 小时。密钥硬编码在 `JwtUtils` 中。使用 **JJWT 0.9.1**（旧版 `io.jsonwebtoken`），在 JDK 21 上需要 `jaxb-api`/`jaxb-runtime` 依赖。
- **Lombok**：广泛使用 `@Data`、`@Slf4j`、`@NoArgsConstructor`、`@AllArgsConstructor`。
- **命名规范**：混合风格 —— 较新的类使用 PascalCase（`AuthService`、`PermissionInterceptor`），较旧的类使用 camelCase（`userService`、`bomController`）。扩展现有模块时遵循该模块的既有风格。
- **MCP 配置**：`.mcp.json` 注册了 PlantUML 绘图服务（`draw-uml-mcp`），用于生成架构图。

## 数据库

- **数据库**：MySQL，远程地址 `mysql5.sqlpub.com:3310`，库名 `smart_workshop`
- **无数据库迁移框架**：不使用 Flyway/Liquibase。DDL 脚本手动管理，位于 `docx/详细设计/` 目录。AI 模块表（`ai_chat_session`、`ai_chat_message`、`ai_audit_log`、`ai_tool_registry`）的 DDL 在 `docx/详细设计/09-AI模块数据库DDL.sql`。
- **status_history 表**：所有状态变更的审计记录，由 `AuditService` 写入。

## 注意事项

- **输出语言**：中文
- **代码注释**：生成的代码必须包含中文注释（类、方法、字段等）
- **Logback 路径**：`logback.xml` 中 FILE appender 使用硬编码的 Windows 绝对路径，在其他平台运行需修改
- **AI_API_KEY**：运行 AI 对话功能前必须设置环境变量 `AI_API_KEY`（Anthropic API Key）
- **设计文档**：位于 `docx/` 目录（已解注 gitignore，会被 Git 追踪），包含需求规格、概要设计、详细设计、API 文档等
