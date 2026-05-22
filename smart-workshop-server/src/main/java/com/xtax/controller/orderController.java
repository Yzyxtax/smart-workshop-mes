package com.xtax.controller;

import com.xtax.entity.ProductionOrder;
import com.xtax.vo.Result;
import com.xtax.service.serviceImpl.orderServiceImpl;
//import com.xtax.service.serviceImpl.orderStateServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/order")
public class orderController {
//    @Autowired
//    private orderStateServiceImpl orderStateServiceImpl;
    @Autowired
    private orderServiceImpl orderServiceImpl;

    @GetMapping("/{planNo}")
    public Result getOrderByPlan(@PathVariable String planNo) {
        log.info("查询计划{}包含的订单", planNo);
        List<ProductionOrder> orderList = orderServiceImpl.getOrderByPlan(planNo);
        return Result.success(orderList);
    }

    @GetMapping
    public Result getAllOrder() {
        log.info("查询所有订单");
        return Result.success(orderServiceImpl.getAllOrder());
    }

//    /**
//     * 统一状态变更接口
//     * @param orderNo 生产订单编号
//     * @param action 动作枚举 (如：PUBLISH, PAUSE, RESUME_WORK)
//     */
//    @PostMapping("/{orderNo}/actions/{action}")
//    public Result handleAction(@PathVariable String orderNo, @PathVariable ActionEnum action, Integer userId) {
//        try {
//            log.info("用户{}申请对订单{}进行{}", userId, orderNo, action);
//            // 调用 Service 层进行状态流转编排
//            // 内部会涉及：权限校验 -> 规则校验 -> 门禁校验 -> 状态执行 -> 审计记录
//            orderStateServiceImpl.handle(orderNo, action, userId);
//
//            return Result.success(String.format("计划 [%s] 执行动作 [%s] 成功", orderNo, action.getDesc()));
//
//        }catch (BusinessException e){
//            //门禁校验失败
//            return Result.error(e.getCode(), e.getMessage());
//        } catch (SecurityException e){
//            // 专门捕获 PermissionPolicy 抛出的权限异常,返回 403 状态码
//            return Result.error(403, "权限不足: " + e.getMessage());
//        } catch (IllegalStateException e) {
//            // 业务逻辑/状态机规则冲突,例如：Plan 状态 [RUNNING] 不允许执行动作 [PUBLISH]
//            return Result.error(400, "操作不符合规则: " + e.getMessage());
//
//        } catch (IllegalArgumentException e) {
//            // 参数错误
//            return Result.error(400, "参数错误: " + e.getMessage());
//        } catch (Exception e) {
//            // 未知系统异常
//            log.error("真实异常类型: {}", e.getClass().getName());
//            return Result.error(500, "系统内部执行异常，请联系管理员");
//        }
//    }
}
