# POJO包类类别分析

## 概述

本文档对 `com.xtax.pojo` 包下的所有类进行类别分析，根据业务用途将其分为以下三类：

| 类别 | 说明 | 命名特征 |
|------|------|----------|
| **Entity（实体类）** | 与数据库表直接映射的核心业务实体，承载业务数据 | 通常以业务领域名称命名，如 User、Bom、Equipment |
| **VO（视图对象）** | 用于视图层展示或API返回结果封装 | 以 VO 结尾或用于数据展示的结构类 |
| **DTO（数据传输对象）** | 用于服务层之间或系统间的数据传输，包括查询参数 | 以 DTO 结尾或 QueryParam 结尾 |

---

## 类别分析结果

### 1. Entity（实体类）

| 序号 | 类名 | 文件路径 | 业务说明 |
|-----:|------|----------|----------|
| 1 | User | [User.java](file:///C:/Users/yzyxt/Desktop/Smart-Workshop-Lightweight-MES-System/Backend/smart-workshop/smart--workshop/src/main/java/com/xtax/pojo/User.java) | 用户实体，封装用户基本信息 |
| 2 | Bom | [Bom.java](file:///C:/Users/yzyxt/Desktop/Smart-Workshop-Lightweight-MES-System/Backend/smart-workshop/smart--workshop/src/main/java/com/xtax/pojo/Bom.java) | 物料清单实体 |
| 3 | Equipment | [Equipment.java](file:///C:/Users/yzyxt/Desktop/Smart-Workshop-Lightweight-MES-System/Backend/smart-workshop/smart--workshop/src/main/java/com/xtax/pojo/Equipment.java) | 设备实体 |
| 4 | Processes | [Processes.java](file:///C:/Users/yzyxt/Desktop/Smart-Workshop-Lightweight-MES-System/Backend/smart-workshop/smart--workshop/src/main/java/com/xtax/pojo/Processes.java) | 工序实体 |
| 5 | ProcessFlow | [ProcessFlow.java](file:///C:/Users/yzyxt/Desktop/Smart-Workshop-Lightweight-MES-System/Backend/smart-workshop/smart--workshop/src/main/java/com/xtax/pojo/ProcessFlow.java) | 工艺流程实体 |
| 6 | ProductionOrder | [ProductionOrder.java](file:///C:/Users/yzyxt/Desktop/Smart-Workshop-Lightweight-MES-System/Backend/smart-workshop/smart--workshop/src/main/java/com/xtax/pojo/ProductionOrder.java) | 生产订单实体 |
| 7 | ProductionLine | [ProductionLine.java](file:///C:/Users/yzyxt/Desktop/Smart-Workshop-Lightweight-MES-System/Backend/smart-workshop/smart--workshop/src/main/java/com/xtax/pojo/ProductionLine.java) | 生产线实体 |
| 8 | Plan | [Plan.java](file:///C:/Users/yzyxt/Desktop/Smart-Workshop-Lightweight-MES-System/Backend/smart-workshop/smart--workshop/src/main/java/com/xtax/pojo/Plan.java) | 生产计划实体 |
| 9 | WorkOrder | [WorkOrder.java](file:///C:/Users/yzyxt/Desktop/Smart-Workshop-Lightweight-MES-System/Backend/smart-workshop/smart--workshop/src/main/java/com/xtax/pojo/WorkOrder.java) | 工单实体 |
| 10 | WorkStep | [WorkStep.java](file:///C:/Users/yzyxt/Desktop/Smart-Workshop-Lightweight-MES-System/Backend/smart-workshop/smart--workshop/src/main/java/com/xtax/pojo/WorkStep.java) | 工序步骤实体 |
| 11 | WorkTeam | [WorkTeam.java](file:///C:/Users/yzyxt/Desktop/Smart-Workshop-Lightweight-MES-System/Backend/smart-workshop/smart--workshop/src/main/java/com/xtax/pojo/WorkTeam.java) | 工作团队实体 |
| 12 | StatusHistory | [StatusHistory.java](file:///C:/Users/yzyxt/Desktop/Smart-Workshop-Lightweight-MES-System/Backend/smart-workshop/smart--workshop/src/main/java/com/xtax/pojo/StatusHistory.java) | 状态历史实体 |
| 13 | TeamItem | [TeamItem.java](file:///C:/Users/yzyxt/Desktop/Smart-Workshop-Lightweight-MES-System/Backend/smart-workshop/smart--workshop/src/main/java/com/xtax/pojo/TeamItem.java) | 团队项实体 |
| 14 | TeamSkillInfo | [TeamSkillInfo.java](file:///C:/Users/yzyxt/Desktop/Smart-Workshop-Lightweight-MES-System/Backend/smart-workshop/smart--workshop/src/main/java/com/xtax/pojo/TeamSkillInfo.java) | 团队技能信息实体 |
| 15 | FunctionDescription | [FunctionDescription.java](file:///C:/Users/yzyxt/Desktop/Smart-Workshop-Lightweight-MES-System/Backend/smart-workshop/smart--workshop/src/main/java/com/xtax/pojo/FunctionDescription.java) | 功能描述实体 |
| 16 | LoginInfo | [LoginInfo.java](file:///C:/Users/yzyxt/Desktop/Smart-Workshop-Lightweight-MES-System/Backend/smart-workshop/smart--workshop/src/main/java/com/xtax/pojo/LoginInfo.java) | 登录信息实体 |
| 17 | FreeUserName | [FreeUserName.java](file:///C:/Users/yzyxt/Desktop/Smart-Workshop-Lightweight-MES-System/Backend/smart-workshop/smart--workshop/src/main/java/com/xtax/pojo/FreeUserName.java) | 可用用户名实体 |
| 18 | LineCompose | [LineCompose.java](file:///C:/Users/yzyxt/Desktop/Smart-Workshop-Lightweight-MES-System/Backend/smart-workshop/smart--workshop/src/main/java/com/xtax/pojo/LineCompose.java) | 产线组成实体 |

### 2. VO（视图对象）

| 序号 | 类名 | 文件路径 | 说明 |
|-----:|------|----------|------|
| 1 | UserVO | [UserVO.java](file:///C:/Users/yzyxt/Desktop/Smart-Workshop-Lightweight-MES-System/Backend/smart-workshop/smart--workshop/src/main/java/com/xtax/pojo/UserVO.java) | 用户视图对象，继承User，扩展工序名称列表 |
| 2 | MatrixDataVO | [MatrixDataVO.java](file:///C:/Users/yzyxt/Desktop/Smart-Workshop-Lightweight-MES-System/Backend/smart-workshop/smart--workshop/src/main/java/com/xtax/pojo/MatrixDataVO.java) | 矩阵数据视图对象，展示用户工序选择信息 |
| 3 | LineComposeVO | [LineComposeVO.java](file:///C:/Users/yzyxt/Desktop/Smart-Workshop-Lightweight-MES-System/Backend/smart-workshop/smart--workshop/src/main/java/com/xtax/pojo/LineComposeVO.java) | 产线组成视图对象，展示产线与班组关联 |
| 4 | BomTreeData | [BomTreeData.java](file:///C:/Users/yzyxt/Desktop/Smart-Workshop-Lightweight-MES-System/Backend/smart-workshop/smart--workshop/src/main/java/com/xtax/pojo/BomTreeData.java) | BOM树形数据结构，用于前端树形展示 |
| 5 | FlowChartData | [FlowChartData.java](file:///C:/Users/yzyxt/Desktop/Smart-Workshop-Lightweight-MES-System/Backend/smart-workshop/smart--workshop/src/main/java/com/xtax/pojo/FlowChartData.java) | 流程图数据结构，用于前端流程图展示 |
| 6 | EdgeData | [EdgeData.java](file:///C:/Users/yzyxt/Desktop/Smart-Workshop-Lightweight-MES-System/Backend/smart-workshop/smart--workshop/src/main/java/com/xtax/pojo/EdgeData.java) | 边数据结构，描述节点连接关系，用于流程图展示 |
| 7 | Result | [Result.java](file:///C:/Users/yzyxt/Desktop/Smart-Workshop-Lightweight-MES-System/Backend/smart-workshop/smart--workshop/src/main/java/com/xtax/pojo/Result.java) | 统一返回结果封装类，用于API返回 |
| 8 | ResultPage | [ResultPage.java](file:///C:/Users/yzyxt/Desktop/Smart-Workshop-Lightweight-MES-System/Backend/smart-workshop/smart--workshop/src/main/java/com/xtax/pojo/ResultPage.java) | 分页结果封装类，用于分页查询返回 |

### 3. DTO（数据传输对象）

| 序号 | 类名 | 文件路径 | 说明 |
|-----:|------|----------|------|
| 1 | MatrixDataDTO | [MatrixDataDTO.java](file:///C:/Users/yzyxt/Desktop/Smart-Workshop-Lightweight-MES-System/Backend/smart-workshop/smart--workshop/src/main/java/com/xtax/pojo/MatrixDataDTO.java) | 矩阵数据传输对象，传输团队成员工序矩阵数据 |
| 2 | FlowChartDataDTO | [FlowChartDataDTO.java](file:///C:/Users/yzyxt/Desktop/Smart-Workshop-Lightweight-MES-System/Backend/smart-workshop/smart--workshop/src/main/java/com/xtax/pojo/FlowChartDataDTO.java) | 流程图数据传输对象，传输节点和边数据 |
| 3 | UserQueryParam | [UserQueryParam.java](file:///C:/Users/yzyxt/Desktop/Smart-Workshop-Lightweight-MES-System/Backend/smart-workshop/smart--workshop/src/main/java/com/xtax/pojo/UserQueryParam.java) | 用户查询参数，用于用户列表查询 |
| 4 | EquipmentQueryParam | [EquipmentQueryParam.java](file:///C:/Users/yzyxt/Desktop/Smart-Workshop-Lightweight-MES-System/Backend/smart-workshop/smart--workshop/src/main/java/com/xtax/pojo/EquipmentQueryParam.java) | 设备查询参数，用于设备列表查询 |
| 5 | ProcessQueryParam | [ProcessQueryParam.java](file:///C:/Users/yzyxt/Desktop/Smart-Workshop-Lightweight-MES-System/Backend/smart-workshop/smart--workshop/src/main/java/com/xtax/pojo/ProcessQueryParam.java) | 工序查询参数，用于工序列表查询 |
| 6 | WorkStepQueryParam | [WorkStepQueryParam.java](file:///C:/Users/yzyxt/Desktop/Smart-Workshop-Lightweight-MES-System/Backend/smart-workshop/smart--workshop/src/main/java/com/xtax/pojo/WorkStepQueryParam.java) | 工序步骤查询参数，用于工序步骤列表查询 |

---

## 类别统计

| 类别 | 数量 | 占比 |
|------|-----:|-----:|
| **Entity** | 18 | 58.1% |
| **VO** | 8 | 25.8% |
| **DTO** | 6 | 19.4% |
| **总计** | **31** | **100%** |

---

## 命名规范

当前项目遵循以下命名规范：

| 类别 | 命名模式 | 示例 |
|------|----------|------|
| Entity | 直接使用业务名称 | User、Bom、Equipment |
| VO | 业务名称 + VO 或以 Data 结尾 | UserVO、MatrixDataVO、BomTreeData |
| DTO | 业务名称 + DTO 或以 QueryParam 结尾 | MatrixDataDTO、UserQueryParam |