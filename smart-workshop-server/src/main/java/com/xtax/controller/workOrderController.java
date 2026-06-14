package com.xtax.controller;

import com.xtax.dto.WorkOrderDTO;
import com.xtax.dto.WorkOrderUpdateDTO;
import com.xtax.entity.WorkOrder;
import com.xtax.enums.ActionEnum;
import com.xtax.exception.BusinessException;
import com.xtax.service.serviceImpl.workOrderServiceImpl;
import com.xtax.service.serviceImpl.workOrderStateServiceImpl;
import com.xtax.utils.JwtUtils;
import com.xtax.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 工单控制器
 * 提供工单 CRUD、状态变更等 REST API
 *
 * 基础路径: /workOrder
 * 认证方式: JWT Token
 */
@Slf4j
@RestController
@RequestMapping("/workOrder")
public class workOrderController {

    /** 用户可直接调用的 Action 白名单（PUBLISH / CANCEL_PUBLISH 仅限系统内部联动触发） */
    private static final Set<ActionEnum> USER_ALLOWED_ACTIONS =
            EnumSet.of(ActionEnum.START_WORK, ActionEnum.FINISH_WORK,
                       ActionEnum.PAUSE, ActionEnum.RESUME, ActionEnum.TERMINATE);

    @Autowired
    private workOrderServiceImpl workOrderServiceImpl;
    @Autowired
    private workOrderStateServiceImpl workOrderStateServiceImpl;

    // ==================== 查询接口 ====================

    /**
     * 查询所有工单
     * GET /workOrder
     */
    @GetMapping
    public Result getAllWorkOrders() {
        log.info("查询所有工单");
        List<WorkOrder> workOrderList = workOrderServiceImpl.getAllWorkOrders();
        return Result.success(workOrderList);
    }

    /**
     * 按订单编号查询工单列表
     * GET /workOrder/order/{orderNo}
     * 注意：此路径必须在 /{workOrderNo} 之前声明，避免被路径变量捕获
     */
    @GetMapping("/order/{orderNo}")
    public Result getWorkOrderByOrder(@PathVariable String orderNo) {
        log.info("查询订单 {} 所包含的工单信息", orderNo);
        try {
            List<WorkOrder> workOrderList = workOrderServiceImpl.getWorkOrderByOrder(orderNo);
            return Result.success(workOrderList);
        } catch (BusinessException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 按人员查询工单列表
     * GET /workOrder/user/{userId}
     * 注意：此路径必须在 /{workOrderNo} 之前声明
     */
    @GetMapping("/user/{userId}")
    public Result getWorkOrderByUser(@PathVariable Integer userId) {
        log.info("查询人员 ID {} 所负责的工单", userId);
        try {
            List<WorkOrder> workOrderList = workOrderServiceImpl.getWorkOrderByUser(userId);
            return Result.success(workOrderList);
        } catch (BusinessException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 按工序查询工单列表
     * GET /workOrder/process/{processId}
     * 注意：此路径必须在 /{workOrderNo} 之前声明
     */
    @GetMapping("/process/{processId}")
    public Result getWorkOrderByProcess(@PathVariable Integer processId) {
        log.info("查询工序 ID {} 下的工单", processId);
        try {
            List<WorkOrder> workOrderList = workOrderServiceImpl.getWorkOrderByProcess(processId);
            return Result.success(workOrderList);
        } catch (BusinessException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 按工单编号查询
     * GET /workOrder/{workOrderNo}
     */
    @GetMapping("/{workOrderNo}")
    public Result getWorkOrderByNo(@PathVariable String workOrderNo) {
        log.info("查询工单: {}", workOrderNo);
        try {
            WorkOrder workOrder = workOrderServiceImpl.getWorkOrderByNo(workOrderNo);
            return Result.success(workOrder);
        } catch (BusinessException e) {
            return Result.error(400, e.getMessage());
        }
    }

    // ==================== 新增接口 ====================

    /**
     * 新增工单（手工创建）
     * POST /workOrder
     * 权限：车间主任 / 生产主管
     */
    @PostMapping
    public Result addWorkOrder(@RequestBody WorkOrderDTO dto) {
        log.info("新增工单: {}", dto);
        try {
            WorkOrder workOrder = workOrderServiceImpl.addWorkOrder(dto);
            return Result.success(workOrder);
        } catch (BusinessException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("新增工单异常: {}", e.getClass().getName(), e);
            return Result.error(500, "系统内部执行异常，请联系管理员");
        }
    }

    // ==================== 修改接口 ====================

    /**
     * 修改工单（状态感知更新）
     * PUT /workOrder/{workOrderNo}
     * 权限：见状态感知规则
     */
    @PutMapping("/{workOrderNo}")
    public Result updateWorkOrder(@PathVariable String workOrderNo, @RequestBody WorkOrderUpdateDTO dto) {
        log.info("修改工单 {}: {}", workOrderNo, dto);
        try {
            workOrderServiceImpl.updateWorkOrder(workOrderNo, dto);
            return Result.success("工单 [" + workOrderNo + "] 修改成功");
        } catch (BusinessException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("修改工单异常: {}", e.getClass().getName(), e);
            return Result.error(500, "系统内部执行异常，请联系管理员");
        }
    }

    // ==================== 删除接口 ====================

    /**
     * 删除工单（仅 CREATED 状态可删）
     * DELETE /workOrder/{workOrderNo}
     * 权限：车间主任 / 生产主管
     */
    @DeleteMapping("/{workOrderNo}")
    public Result deleteWorkOrder(@PathVariable String workOrderNo) {
        log.info("删除工单: {}", workOrderNo);
        try {
            workOrderServiceImpl.deleteWorkOrder(workOrderNo);
            return Result.success("工单 [" + workOrderNo + "] 删除成功");
        } catch (BusinessException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("删除工单异常: {}", e.getClass().getName(), e);
            return Result.error(500, "系统内部执行异常，请联系管理员");
        }
    }

    // ==================== 状态变更接口 ====================

    /**
     * 统一状态变更接口
     * POST /workOrder/{workOrderNo}/actions/{action}
     * 从请求头 JWT Token 中解析 userId
     *
     * @param workOrderNo 工单编号
     * @param action      动作枚举 (如：PUBLISH, START_WORK, PAUSE, RESUME, FINISH_WORK, TERMINATE 等)
     */
    @PostMapping("/{workOrderNo}/actions/{action}")
    public Result handleAction(@PathVariable String workOrderNo, @PathVariable ActionEnum action,
                               HttpServletRequest request) {
        // 校验 action 是否在用户允许集合中，PUBLISH/CANCEL_PUBLISH 仅限系统内部联动触发
        if (!USER_ALLOWED_ACTIONS.contains(action)) {
            return Result.error(400, "操作 [" + action.getDesc() + "] 仅限系统内部触发，不支持直接 API 调用");
        }
        try {
            Integer userId = extractUserIdFromToken(request);
            log.info("用户 {} 申请对工单 {} 进行 {}", userId, workOrderNo, action.getDesc());
            workOrderStateServiceImpl.handle(workOrderNo, action, userId);
            return Result.success(String.format("工单 [%s] 执行动作 [%s] 成功", workOrderNo, action.getDesc()));
        } catch (BusinessException e) {
            return Result.error(400, e.getMessage());
        } catch (SecurityException e) {
            return Result.error(403, "权限不足: " + e.getMessage());
        } catch (IllegalStateException e) {
            return Result.error(400, "操作不符合规则: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return Result.error(400, "参数错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("状态变更异常 - 类型: {}", e.getClass().getName(), e);
            return Result.error(500, "系统内部执行异常，请联系管理员");
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 从 HTTP 请求头中提取 JWT Token 并解析出 userId
     * 支持 Authorization: Bearer xxx 和 token: xxx 两种传递方式
     */
    private Integer extractUserIdFromToken(HttpServletRequest request) {
        String token = null;
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        if (token == null || token.isEmpty()) {
            token = request.getHeader("token");
        }
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("未提供有效的认证令牌");
        }
        Map<String, Object> claims = JwtUtils.parseToken(token);
        return (Integer) claims.get("id");
    }
}
