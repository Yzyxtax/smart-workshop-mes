package com.xtax.controller;

import com.xtax.dto.OrderDTO;
import com.xtax.dto.OrderUpdateDTO;
import com.xtax.entity.ProductionOrder;
import com.xtax.entity.WorkOrder;
import com.xtax.enums.ActionEnum;
import com.xtax.exception.BusinessException;
import com.xtax.service.serviceImpl.orderServiceImpl;
import com.xtax.service.serviceImpl.orderStateServiceImpl;
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
 * 生产订单控制器
 * 提供订单 CRUD、状态变更、联动查询等 REST API
 *
 * 基础路径: /order
 * 认证方式: JWT Token
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class orderController {

    /** 用户可直接调用的 Action 白名单（PUBLISH / CANCEL_PUBLISH / START_WORK / FINISH_WORK 仅限系统内部联动触发） */
    private static final Set<ActionEnum> USER_ALLOWED_ACTIONS =
            EnumSet.of(ActionEnum.PAUSE, ActionEnum.RESUME, ActionEnum.TERMINATE);

    @Autowired
    private orderStateServiceImpl orderStateServiceImpl;
    @Autowired
    private orderServiceImpl orderServiceImpl;

    // ==================== 查询接口 ====================

    /**
     * 查询所有订单
     * GET /order
     */
    @GetMapping
    public Result getAllOrder() {
        log.info("查询所有订单");
        List<ProductionOrder> orderList = orderServiceImpl.getAllOrder();
        return Result.success(orderList);
    }

    /**
     * 按计划编号查询订单列表
     * GET /order/plan/{planNo}
     * 注意：此路径必须在 /{orderNo} 之前声明，避免被路径变量捕获
     */
    @GetMapping("/plan/{planNo}")
    public Result getOrderByPlan(@PathVariable String planNo) {
        log.info("查询计划 {} 包含的订单", planNo);
        List<ProductionOrder> orderList = orderServiceImpl.getOrderByPlan(planNo);
        return Result.success(orderList);
    }

    /**
     * 按订单编号查询
     * GET /order/{orderNo}
     */
    @GetMapping("/{orderNo}")
    public Result getOrderByNo(@PathVariable String orderNo) {
        log.info("查询订单: {}", orderNo);
        try {
            ProductionOrder order = orderServiceImpl.getOrderByNo(orderNo);
            return Result.success(order);
        } catch (BusinessException e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 查询订单下的工单列表
     * GET /order/{orderNo}/workOrders
     */
    @GetMapping("/{orderNo}/workOrders")
    public Result getWorkOrders(@PathVariable String orderNo) {
        log.info("查询订单 {} 下的工单", orderNo);
        try {
            List<WorkOrder> workOrders = orderServiceImpl.getWorkOrdersByOrderNo(orderNo);
            return Result.success(workOrders);
        } catch (BusinessException e) {
            return Result.error(400, e.getMessage());
        }
    }

    // ==================== 新增接口 ====================

    /**
     * 新增订单
     * POST /order
     * 权限：车间主任 / 生产主管
     */
    @PostMapping
    public Result addOrder(@RequestBody OrderDTO dto) {
        log.info("新增订单: {}", dto);
        try {
            ProductionOrder order = orderServiceImpl.addOrder(dto);
            return Result.success(order);
        } catch (BusinessException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("新增订单异常: {}", e.getClass().getName(), e);
            return Result.error(500, "系统内部执行异常，请联系管理员");
        }
    }

    // ==================== 修改接口 ====================

    /**
     * 修改订单（状态感知更新）
     * PUT /order/{orderNo}
     * 权限：车间主任 / 生产主管
     */
    @PutMapping("/{orderNo}")
    public Result updateOrder(@PathVariable String orderNo, @RequestBody OrderUpdateDTO dto) {
        log.info("修改订单 {}: {}", orderNo, dto);
        try {
            orderServiceImpl.updateOrder(orderNo, dto);
            return Result.success("订单 [" + orderNo + "] 修改成功");
        } catch (BusinessException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("修改订单异常: {}", e.getClass().getName(), e);
            return Result.error(500, "系统内部执行异常，请联系管理员");
        }
    }

    // ==================== 删除接口 ====================

    /**
     * 删除订单（仅 CREATED 状态可删）
     * DELETE /order/{orderNo}
     * 权限：车间主任 / 生产主管
     */
    @DeleteMapping("/{orderNo}")
    public Result deleteOrder(@PathVariable String orderNo) {
        log.info("删除订单: {}", orderNo);
        try {
            orderServiceImpl.deleteOrder(orderNo);
            return Result.success("订单 [" + orderNo + "] 删除成功");
        } catch (BusinessException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("删除订单异常: {}", e.getClass().getName(), e);
            return Result.error(500, "系统内部执行异常，请联系管理员");
        }
    }

    // ==================== 状态变更接口 ====================

    /**
     * 统一状态变更接口
     * POST /order/{orderNo}/actions/{action}
     * 从请求头 JWT Token 中解析 userId
     *
     * @param orderNo 生产订单编号
     * @param action  动作枚举 (如：PUBLISH, PAUSE, RESUME, TERMINATE 等)
     */
    @PostMapping("/{orderNo}/actions/{action}")
    public Result handleAction(@PathVariable String orderNo, @PathVariable ActionEnum action,
                               HttpServletRequest request) {
        // 校验 action 是否在用户允许集合中，PUBLISH/CANCEL_PUBLISH/START_WORK/FINISH_WORK 仅限系统内部联动触发
        if (!USER_ALLOWED_ACTIONS.contains(action)) {
            return Result.error(400, "操作 [" + action.getDesc() + "] 仅限系统内部触发，不支持直接 API 调用");
        }
        try {
            Integer userId = extractUserIdFromToken(request);
            log.info("用户 {} 申请对订单 {} 进行 {}", userId, orderNo, action.getDesc());
            orderStateServiceImpl.handle(orderNo, action, userId);
            return Result.success(String.format("订单 [%s] 执行动作 [%s] 成功", orderNo, action.getDesc()));
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
