---
name: uml-generator
description: 为本项目（Java Spring Boot 智能制造 MES 系统）生成 PlantUML 类图、时序图、状态图等。Claude 负责读取源码生成 PlantUML，draw-uml MCP Server 负责渲染为 PNG/SVG 图片。
metadata:
  type: project
  tags: [uml, plantuml, diagram, class-diagram, sequence-diagram, state-machine]
---

# UML 图生成器 — 智能制造 MES 系统

## 概述

本 Skill 指导 Claude 为此 Java 多模块 MES 项目生成专业的 UML 图表。工作流程：**Claude 阅读源码 → 生成 PlantUML → draw-uml MCP 渲染为图片**。

## 项目架构速览

```
smart-workshop (多模块 Maven, Java 21, Spring Boot 3.5.7)

smart-workshop-pojo/         — 领域对象层
  ├── entity/                — 实体类 (Order, Plan, WorkOrder, User, Role, ...)
  ├── dto/                   — 数据传输对象
  ├── vo/                    — 视图对象 (Result, ResultPage)
  └── enums/                 — 枚举 (StateEnum, ActionEnum)

smart-workshop-common/       — 共享基础设施
  ├── JwtUtils               — JWT 工具 (HS256, 24h过期)
  ├── CorsConfig             — 跨域配置
  └── globalExceptionHandler — 全局异常处理

smart-workshop-production/   — 核心生产领域
  ├── orderStateMachine      — 订单状态机 (CREATED→RELEASED→RUNNING→PAUSED/COMPLETED)
  ├── planStateMachine       — 计划状态机
  ├── workOrderStateMachine  — 工单状态机
  ├── StateContext           — 状态上下文 (bizNo, action, userId)
  └── PermissionPolicy       — 权限策略接口

smart-workshop-server/       — Web 层
  ├── controller/            — REST 控制器 (RESTful 动作分发模式)
  ├── service/serviceImpl/   — 业务逻辑
  ├── mapper/                — MyBatis 映射器
  ├── filter/                — LoginFilter (JWT认证)
  ├── interceptor/           — PermissionInterceptor (RBAC权限)
  └── audit/                 — 审计服务
```

## 常用图表场景

### 1. 类图（Class Diagram）
生成某领域的类关系图时，必须读取相关 Java 文件，提取：
- 类名、字段、方法（带访问修饰符）
- 继承/实现关系（extends/implements）
- 关联关系（@Autowired、构造函数注入、集合字段）

### 2. 时序图（Sequence Diagram）
生成请求处理时序图时，覆盖完整链路：
`LoginFilter → PermissionInterceptor → Controller → Service → Mapper → DB`

### 3. 状态图（State Diagram）
状态转换图：`CREATED → RELEASED → RUNNING → PAUSED/COMPLETED`
使用 PlantUML state 语法，标注每个转换对应的 ActionEnum。

### 4. 组件图（Component Diagram）
模块依赖关系：`server → production → common → pojo`

## PlantUML 风格约定

- 所有图表使用 `skinparam` 统一中文字体
- 类图使用 `+` 表示 public，`-` 表示 private
- 时序图标注关键方法调用和返回值
- 状态图用颜色区分终态（COMPLETED=green, PAUSED=orange）

## 使用方式

直接告诉 Claude：
- "生成 Order 领域的类图"
- "画登录认证的时序图"
- "画订单状态机的状态转换图"
- "画整个项目的模块依赖图"

Claude 会自动读取源码 → 生成 PlantUML → 调用 draw-uml MCP 渲染为图片。
