# 智联车间轻量化MES系统 — RBAC权限系统详细设计

> **版本**: v1.0  
> **日期**: 2026-06-11  
> **文档类型**: 详细设计文档  
> **来源**: 逆向工程自 RBAC 权限管理系统接口设计文档

---

## 目录

1. [设计目标](#1-设计目标)
2. [权限模型](#2-权限模型)
3. [数据模型设计](#3-数据模型设计)
4. [权限编码规范](#4-权限编码规范)
5. [认证与授权流程](#5-认证与授权流程)
6. [@RequirePermission 注解设计](#6-requirepermission-注解设计)
7. [权限拦截器设计](#7-权限拦截器设计)
8. [服务层设计](#8-服务层设计)
9. [接口设计](#9-接口设计)
10. [权限配置表](#10-权限配置表)
11. [两层权限控制体系](#11-两层权限控制体系)

---

## 1. 设计目标

### 1.1 核心目标

| 目标 | 说明 |
|------|------|
| **角色基权限** | 用户→角色→权限 三级映射，权限通过角色间接授予 |
| **细粒度控制** | 支持按模块(module)和操作(action)的权限编码精确控制 |
| **声明式校验** | 通过 `@RequirePermission` 注解声明所需权限，拦截器自动校验 |
| **策略式校验** | 生产域通过 `PermissionPolicy` 在服务层按 action 类型实施策略校验 |

### 1.2 权限模型选型: RBAC (Role-Based Access Control)

```
      ┌────────┐      N:M       ┌────────┐      N:M       ┌────────────┐
      │  User  │ ←──────────→  │  Role  │ ←──────────→ │ Permission │
      └────────┘  user_role     └────────┘ role_permission └────────────┘
```

---

## 2. 权限模型

### 2.1 五张核心表

| 表 | 角色 | 说明 |
|----|------|------|
| `users` | 用户主体 | 登录用户，通过 `user_role` 关联角色 |
| `role` | 角色定义 | 角色编码+名称+描述 |
| `permission` | 权限定义 | 权限编码+名称+模块，由初始化脚本固定 |
| `user_role` | 用户-角色关联 | N:M 关联表 |
| `role_permission` | 角色-权限关联 | N:M 关联表 |

### 2.2 预置角色

| ID | role_code | role_name | 职责范围 |
|----|-----------|-----------|---------|
| 1 | ROLE_PROD_MANAGER | 生产主管 | 生产计划制定、发布、管理 |
| 2 | ROLE_PROCESS_ENGINEER | 工艺工程师 | 工艺流程设计、BOM管理 |
| 3 | ROLE_HR_MANAGER | 人事主管 | 人员管理、岗位分配、权限分配 |
| 4 | ROLE_WORKSHOPS_DIRECTOR | 车间主任 | 车间整体管理、生产调度 |
| 5 | ROLE_TEAM_LEADER | 班组长 | 班组管理、工单分派 |

### 2.3 权限查询路径 (SQL)

```sql
-- 查询用户的所有权限编码
SELECT DISTINCT p.permission_code
FROM users u
JOIN user_role ur ON u.id = ur.user_id
JOIN role_permission rp ON ur.role_id = rp.role_id
JOIN permission p ON rp.permission_id = p.id
WHERE u.id = #{userId}
```

---

## 3. 数据模型设计

### 3.1 Permission — 权限表

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Permission {
    private Integer id;
    private String permissionCode;   // 权限编码（如 PLAN_CREATE）
    private String permissionName;   // 中文名称
    private String module;           // 所属模块（SYS/PLAN/ORDER/...）
    private String description;      // 描述
    private LocalDateTime createdAt; // 创建时间
}
```

### 3.2 Role — 角色表

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    private Integer id;
    private String roleCode;         // 角色编码（如 ROLE_PROD_MANAGER）
    private String roleName;         // 角色名称（中文）
    private String description;      // 描述
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### 3.3 UserRole — 用户-角色关联

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRole {
    private Integer id;
    private Integer userId;
    private Integer roleId;
    private LocalDateTime createdAt;
}
```

### 3.4 RolePermission — 角色-权限关联

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePermission {
    private Integer id;
    private Integer roleId;
    private Integer permissionId;
    private LocalDateTime createdAt;
}
```

### 3.5 VO 类设计

| VO 类 | 用途 | 关键字段 |
|-------|------|---------|
| `RoleVO` | 角色详情展示 | id, roleCode, roleName, description, permissions, permissionCount |
| `PermissionVO` | 权限展示 | id, permissionCode, permissionName, module |
| `UserPermissionVO` | 用户权限汇总 | userId, username, name, roles, permissions(List\<String\>) |

### 3.6 DTO 类设计

| DTO 类 | 用途 |
|--------|------|
| `AssignRoleDTO` | 为用户分配角色: userId + roleIds(List) |
| `AssignPermissionDTO` | 为角色分配权限: roleId + permissionIds(List) |
| `RoleQueryParam` | 角色查询参数: page, pageSize, roleName |

---

## 4. 权限编码规范

### 4.1 权限编码格式

```
{MODULE}_{ACTION}
```

**MODULE（模块编码）**:

| 编码 | 模块 | 说明 |
|------|------|------|
| SYS | 系统管理 | 用户、角色、权限管理 |
| PLAN | 生产计划 | 计划管理 |
| ORDER | 订单管理 | 订单管理 |
| WORKORDER | 工单管理 | 工单管理 |
| EQU | 设备管理 | 设备管理 |
| BOM | 物料清单 | BOM管理 |
| PROC | 工序管理 | 工序、工艺流程管理 |

**ACTION（操作编码）**:

| 编码 | 说明 |
|------|------|
| CREATE | 创建 |
| UPDATE | 修改 |
| DELETE | 删除 |
| QUERY | 查询 |
| ASSIGN | 分配 |
| MANAGE | 管理（综合管理权限） |
| EXPORT | 导出 |
| IMPORT | 导入 |

**示例**: `ORDER_CREATE`、`SYS_USER_DELETE`、`SYS_PERMISSION_ASSIGN`

### 4.2 角色编码格式

```
ROLE_{DOMAIN}_{ROLE_NAME}
```

**DOMAIN**: PROD(生产)、PROCESS(工艺)、HR(人事)、WORKSHOPS(车间)、TEAM(班组)

**示例**: `ROLE_PROD_MANAGER`、`ROLE_QUALITY_INSPECTOR`

### 4.3 编码管理规则

- **权限编码**: 由数据库初始化脚本定义，**不提供运行时 API 创建/修改/删除**
- **角色编码**: 可通过 `/role` 接口动态创建，但需遵循 `ROLE_` 前缀约定
- **全局唯一**: 权限编码和角色编码均通过数据库 UNIQUE 约束保证全局唯一

---

## 5. 认证与授权流程

### 5.1 整体认证链

```
HTTP Request
  │
  ▼
LoginFilter (@WebFilter "/*")
  ├── /login → 放行
  └── 其他 → 提取 JWT → 验证
       ├── 失败 → 401
       └── 成功 → 放行
  │
  ▼
PermissionInterceptor (注册到 /**)
  ├── 排除: /login, /error, /auth/**, /permission/**, /role/**
  ├── 检查 @RequirePermission 注解
  │   ├── 无注解 → 放行
  │   └── 有注解:
  │       ├── 从 Token 解析 userId
  │       ├── AuthService.getUserPermissionCodes(userId)
  │       │   └── SELECT DISTINCT p.permission_code
  │       │       FROM users u
  │       │       JOIN user_role ur ON u.id = ur.user_id
  │       │       JOIN role_permission rp ON ur.role_id = rp.role_id
  │       │       JOIN permission p ON rp.permission_id = p.id
  │       │       WHERE u.id = ?
  │       ├── 按 AND/OR 逻辑校验
  │       │   ├── AND: 用户必须拥有所有声明的权限
  │       │   └── OR: 用户拥有任意一个即可
  │       ├── 失败 → 403
  │       └── 成功 → 放行
  │
  ▼
Controller
```

### 5.2 登录流程

```
POST /login { userName, password }
  ├── userMapper.getByUserName(userName)
  ├── 密码匹配校验
  ├── JwtUtils.generateToken(claims{id: userId})
  └── 返回 LoginInfo { id, userName, name, token }
```

### 5.3 权限查询接口

```
GET /auth/permissions  (需已登录)
  └── 返回: { userId, username, name, roles: [...], permissions: [...] }

GET /auth/check?permissionCode=PLAN_CREATE  (需已登录)
  └── 返回: { hasPermission: true/false }
```

---

## 6. @RequirePermission 注解设计

### 6.1 注解定义

```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /** 所需权限编码数组 */
    String[] value() default {};

    /** 校验逻辑: AND（需全部满足）或 OR（满足其一即可） */
    Logical logical() default Logical.AND;
}

public enum Logical {
    AND,  // 所有权限都必须拥有
    OR    // 拥有任意一个即可
}
```

### 6.2 使用示例

```java
// 单权限
@RequirePermission("SYS_USER_CREATE")
@PostMapping("/user")
public Result addUser(@RequestBody User user) { ... }

// 多权限（OR: 拥有任意一个即可）
@RequirePermission(value = {"SYS_ROLE_MANAGE", "SYS_PERMISSION_ASSIGN"}, logical = Logical.OR)
@PostMapping("/role")
public Result addRole(@RequestBody Role role) { ... }

// 类级别（该类所有方法共享）
@RequirePermission("SYS_USER_MANAGE")
@RestController
public class UserController { ... }
```

### 6.3 注解处理优先级

1. 方法上的 `@RequirePermission` 注解优先于类上的注解
2. 如果方法有注解，则使用方法注解的逻辑；否则使用类注解的逻辑

---

## 7. 权限拦截器设计

### 7.1 PermissionInterceptor

```java
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Autowired
    private AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        // 1. 仅处理 HandlerMethod（不处理静态资源）
        if (!(handler instanceof HandlerMethod)) return true;

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 2. 获取注解（先方法后类）
        RequirePermission annotation = handlerMethod.getMethodAnnotation(RequirePermission.class);
        if (annotation == null) {
            annotation = handlerMethod.getBeanType().getAnnotation(RequirePermission.class);
        }
        if (annotation == null) return true; // 无需权限校验

        // 3. 解析 Token 获取 userId
        Integer userId = getUserIdFromToken(request);

        // 4. 获取用户所有权限编码
        Set<String> userPermissions = authService.getUserPermissionCodes(userId);

        // 5. 按 AND/OR 逻辑校验
        String[] required = annotation.value();
        boolean pass;
        if (annotation.logical() == Logical.AND) {
            pass = Arrays.stream(required).allMatch(userPermissions::contains);
        } else {
            pass = Arrays.stream(required).anyMatch(userPermissions::contains);
        }

        if (!pass) {
            response.setStatus(403);
            response.getWriter().write("{\"code\":403,\"message\":\"权限不足\"}");
            return false;
        }
        return true;
    }
}
```

### 7.2 拦截器注册

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private PermissionInterceptor permissionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/login",
                    "/error",
                    "/permission/**",
                    "/role/**",
                    "/auth/**"
                );
    }
}
```

### 7.3 排除路径策略

| 路径模式 | 排除原因 |
|----------|---------|
| `/login` | 认证入口，无需权限 |
| `/error` | Spring 错误页 |
| `/auth/**` | 权限查询接口自身，仅需 JWT 认证 |
| `/permission/**` | 权限查询，GET 放行 |
| `/role/**` | 角色查询，GET 放行（写操作通过注解保护） |

---

## 8. 服务层设计

### 8.1 AuthService

```java
public interface AuthService {
    /** 获取当前用户完整权限信息（含角色列表） */
    UserPermissionVO getCurrentUserPermissions(Integer userId);

    /** 检查用户是否拥有指定权限 */
    boolean checkPermission(Integer userId, String permissionCode);

    /** 获取用户所有权限编码集合 */
    Set<String> getUserPermissionCodes(Integer userId);
}
```

### 8.2 RoleService

```java
public interface RoleService {
    ResultPage<Role> getAllRole(RoleQueryParam param);  // 分页查询
    RoleVO getRoleById(Integer id);                      // 详情（含权限列表）
    int addRole(Role role);                              // 创建
    int updateRole(Role role);                           // 更新
    int deleteRoles(List<Integer> ids);                  // 批量删除（级联删除 role_permission）
}
```

### 8.3 UserRoleService

```java
public interface UserRoleService {
    List<RoleVO> getRolesByUserId(Integer userId);                     // 查用户角色
    List<UserVO> getUsersByRoleId(Integer roleId);                     // 查角色用户
    int assignRoles(AssignRoleDTO dto);                                // 分配角色
    int removeRoles(Integer userId, List<Integer> roleIds);            // 移除角色
}
```

### 8.4 RolePermissionService

```java
public interface RolePermissionService {
    List<PermissionVO> getPermissionsByRoleId(Integer roleId);          // 查角色权限
    List<RoleVO> getRolesByPermissionId(Integer permissionId);          // 查权限所属角色
    int assignPermissions(AssignPermissionDTO dto);                     // 分配权限
    int removePermissions(Integer roleId, List<Integer> permissionIds); // 移除权限
}
```

---

## 9. 接口设计

### 9.1 接口汇总

| 模块 | 路径 | 方法 | 功能 | 权限保护 |
|------|------|------|------|---------|
| **角色管理** | /role | GET | 分页查询角色 | GET 自动放行 |
| | /role/{id} | GET | 角色详情 | GET 自动放行 |
| | /role | POST | 创建角色 | @RequirePermission(SYS_ROLE_MANAGE) |
| | /role | PUT | 更新角色 | @RequirePermission(SYS_ROLE_MANAGE) |
| | /role | DELETE | 批量删除 | @RequirePermission(SYS_ROLE_MANAGE) |
| **权限管理** | /permission | GET | 查询权限列表(按模块筛选) | GET 自动放行 |
| | /permission/{id} | GET | 权限详情 | GET 自动放行 |
| | /permission/grouped | GET | 按模块分组查询 | GET 自动放行 |
| **用户角色** | /user-role/user/{userId} | GET | 查用户角色 | 仅 LoginFilter |
| | /user-role/role/{roleId} | GET | 查角色用户 | 仅 LoginFilter |
| | /user-role | POST | 分配角色 | @RequirePermission(SYS_PERMISSION_ASSIGN) |
| | /user-role | DELETE | 移除角色 | @RequirePermission(SYS_PERMISSION_ASSIGN) |
| **角色权限** | /role-permission/role/{roleId} | GET | 查角色权限 | 仅 LoginFilter |
| | /role-permission/permission/{permissionId} | GET | 查权限所属角色 | 仅 LoginFilter |
| | /role-permission | POST | 分配权限 | @RequirePermission(SYS_PERMISSION_ASSIGN) |
| | /role-permission | DELETE | 移除权限 | @RequirePermission(SYS_PERMISSION_ASSIGN) |
| **权限校验** | /auth/permissions | GET | 查当前用户权限 | 已登录 |
| | /auth/check?permissionCode= | GET | 校验指定权限 | 已登录 |

---

## 10. 权限配置表

### 10.1 接口权限完整配置

| 接口 | 方法 | 所需权限 |
|------|------|---------|
| /user | GET | 无（仅 LoginFilter） |
| /user | POST | SYS_USER_CREATE |
| /user | PUT | SYS_USER_UPDATE |
| /user | DELETE | SYS_USER_DELETE |
| /role | GET | 无（拦截器对 /role/** GET 放行） |
| /role | POST/PUT/DELETE | SYS_ROLE_MANAGE |
| /permission | GET | 无（拦截器对 /permission/** GET 放行） |
| /user-role | GET | 仅 LoginFilter |
| /user-role | POST/DELETE | SYS_PERMISSION_ASSIGN |
| /role-permission | GET | 仅 LoginFilter |
| /role-permission | POST/DELETE | SYS_PERMISSION_ASSIGN |
| /plan | GET | 仅 LoginFilter |
| /plan | POST | PLAN_CREATE **(待补全)** |
| /plan | PUT | PLAN_UPDATE **(待补全)** |
| /plan | DELETE | PLAN_DELETE **(待补全)** |
| /plan/{no}/actions/{action} | POST | PermissionPolicy 策略校验 |
| /order/{no}/actions/{action} | POST | PermissionPolicy 策略校验 |
| /workOrder/{no}/actions/{action} | POST | PermissionPolicy 策略校验 |
| /equipment | POST/PUT/DELETE | EQU_* **(待补全)** |
| /bom | POST/PUT/DELETE | BOM_* **(待补全)** |
| /process | POST/PUT/DELETE | PROC_* **(待补全)** |

---

## 11. 两层权限控制体系

### 11.1 注解层 vs 策略层

系统中存在**两层**权限控制：

| 层级 | 位置 | 方式 | 适用场景 |
|------|------|------|---------|
| **注解层** | Controller | `@RequirePermission(编码)` | 静态权限：CRUD 操作的增删改 |
| **策略层** | Service | `PermissionPolicy.check(ctx, entity)` | 动态权限：状态变更按 action 类型策略判定 |

### 11.2 策略层的使用

策略层用于生产域的状态变更场景，因为同一条 API（`POST /plan/{no}/actions/{action}`）对应多种 action，不同 action 需要不同角色的权限：

```java
// planPermissionPolicy: 检查生产主管权限
public void check(StateContext ctx, Plan plan) {
    // PUBLISH → 需"生产主管"角色
    // CANCEL_PUBLISH → 需"生产主管"角色
    // PAUSE → 需"生产主管"角色
    // RESUME → 需"生产主管"角色
    // TERMINATE → 需"生产主管"角色
}

// workOrderPermissionPolicy: 更复杂的复合逻辑
public void check(StateContext ctx, WorkOrder wo) {
    // START_WORK → 需 userId == wo.userId
    // FINISH_WORK → 需 userId == wo.userId
    // PAUSE → 需 userId == wo.userId 或主管角色
    // RESUME → 需 userId == wo.userId 或主管角色
    // TERMINATE → 需主管角色
}
```

### 11.3 两层的关系

- **注解层** 和 **策略层** 是**互补**关系，不是替代关系:
  - 注解层负责 CRUD 操作的入口权限
  - 策略层负责状态变更的细粒度行为级权限
- 生产域的状态变更接口（`/actions/{action}`）**仅使用策略层**，不添加 `@RequirePermission`
- 生产域 CRUD 接口（`POST/PUT/DELETE /plan`）**计划使用注解层**（目前待补全）

---

## 附录 A: 权限缓存设计（待实现）

### 当前状态

当前每次权限校验均直接查询数据库（三表 JOIN），建议后续引入缓存：

```
Cache Key:   user:permission:{userId}
Cache Value: Set<String> (权限编码集合)
TTL:         30 分钟
失效策略:    用户角色变更 / 角色权限变更时主动清除
```

### 推荐实现

```java
// 使用 Spring Cache 或 Caffeine 本地缓存
@Cacheable(value = "userPermissions", key = "#userId")
public Set<String> getUserPermissionCodes(Integer userId) { ... }

@CacheEvict(value = "userPermissions", key = "#userId")
public int assignRoles(AssignRoleDTO dto) { ... }
```

---

## 附录 B: 权限编码完整清单（预设）

| 模块 | CREATE | UPDATE | DELETE | QUERY | ASSIGN | MANAGE | EXPORT | IMPORT |
|------|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| SYS | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | — | — |
| PLAN | ✅ | ✅ | ✅ | ✅ | — | — | ✅ | ✅ |
| ORDER | ✅ | ✅ | ✅ | ✅ | ✅ | — | ✅ | — |
| WORKORDER | ✅ | ✅ | ✅ | ✅ | ✅ | — | ✅ | — |
| EQU | ✅ | ✅ | ✅ | ✅ | — | — | — | — |
| BOM | ✅ | ✅ | ✅ | ✅ | — | — | — | — |
| PROC | ✅ | ✅ | ✅ | ✅ | — | — | — | — |

> **文档维护**: 新增权限编码或角色时需同步更新数据库初始化脚本和本文档。
