package com.xtax.service.serviceImpl;

import com.xtax.audit.AuditService;
import com.xtax.entity.Plan;
import com.xtax.entity.ProductionOrder;
import com.xtax.entity.WorkOrder;
import com.xtax.enums.ActionEnum;
import com.xtax.enums.StateEnum;
import com.xtax.exception.BusinessException;
import com.xtax.mapper.lineMapper;
import com.xtax.mapper.orderMapper;
import com.xtax.mapper.planMapper;
import com.xtax.mapper.processFlowMapper;
import com.xtax.mapper.workOrderMapper;
import com.xtax.plicy.GatePolicy;
import com.xtax.plicy.planPermissionPolicy;
import com.xtax.service.planStateService;
import com.xtax.stateDomain.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class planStateServiceImpl implements planStateService {
    @Autowired
    private planStateMachine planStateMachine;
    @Autowired
    private planPermissionPolicy permissionPolicy;
    @Autowired
    private GatePolicy gatePolicy;
    @Autowired
    private AuditService auditService;
    @Autowired
    private planMapper planMapper;
    @Autowired
    private orderMapper orderMapper;
    @Autowired
    private processFlowMapper processFlowMapper;
    @Autowired
    private lineMapper lineMapper;
    @Autowired
    private workOrderMapper workOrderMapper;

    /**
     * 统一的状态处理入口
     *
     * @param planNo  计划编号
     * @param action 执行动作
     * @param userId   当前操作用户
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void handle(String planNo, ActionEnum action, Integer userId) {
        // 1. 获取业务对象并构建上下文
        Plan plan = planMapper.getPlanByNo(planNo);
        if (plan == null) {
            throw new IllegalArgumentException("未找到编号为 " + planNo + " 的生产计划");
        }

        StateContext context = new StateContext(planNo, action, userId);
        StateEnum fromStatus = plan.getStatus();

        // 2. 权限校验 (PermissionPolicy)
        permissionPolicy.check(userId, action);

        // 3. 状态机迁移合法性校验 (StateMachine)
        planStateMachine.check(fromStatus, action);

        // 4. 业务门禁校验 (GatePolicy),仅在发布等关键决策型动作时触发
        if (ActionEnum.PUBLISH.equals(action)) {
            gatePolicy.check(context);
        }

        // 5. TERMINATE 额外校验：已有 Order 处于执行中时不允许作废
        if (ActionEnum.TERMINATE.equals(action)) {
            List<ProductionOrder> orders = orderMapper.getOrderByPlan(planNo);
            boolean anyRunning = orders.stream()
                    .anyMatch(o -> o.getStatus() == StateEnum.RUNNING);
            if (anyRunning) {
                throw new BusinessException("计划下存在执行中的订单，不允许作废");
            }
        }

        // 6. 计算并更新新状态
        StateEnum toStatus = fromStatus.next(action);
        plan.setStatus(toStatus);
        planMapper.updatePlanStatus(planNo, toStatus);

        // 7. 审计记录
        auditService.record("PLAN", context, fromStatus, toStatus);

        // 8. 状态联动处理
        handleLinkage(plan, action, userId);
    }

    /**
     * 处理状态联动逻辑
     * Plan PUBLISH → 根据工艺流程和可用产线拆分为生产订单（Order）
     * Plan PAUSE → 级联暂停所有 RUNNING 状态的订单
     * Plan RESUME → 级联恢复所有 PAUSED 状态的订单
     * Plan CANCEL_PUBLISH → 级联作废所有非终态的订单
     *
     * @param plan   计划对象
     * @param action 执行的动作
     * @param userId 触发联动的操作人（透传至订单审计记录）
     */
    public void handleLinkage(Plan plan, ActionEnum action, Integer userId) {
        switch (action) {
            case PUBLISH: {
                // 1. 获取计划对应的工艺流程列表
                Integer bomId = plan.getBomId();
                List<Integer> processFlowIdList = processFlowMapper.getProcessFlowIdList(bomId);

                // 2. 遍历工艺流程，为每条可用产线创建一个生产订单
                for (Integer processFlowId : processFlowIdList) {
                    String lineNo = findLineByProcessFlowId(processFlowId);
                    if (lineNo == null) {
                        log.info("工艺流程 ID={} 无可用的空闲产线，跳过", processFlowId);
                        continue;
                    }

                    // 3. 生成订单编号：计划编号 + 产线编号
                    String orderNo = plan.getPlanNo() + "-" + lineNo;

                    // 4. 创建生产订单
                    ProductionOrder order = new ProductionOrder();
                    order.setOrderNo(orderNo);
                    order.setPlanNo(plan.getPlanNo());
                    order.setLineNo(lineNo);
                    order.setOrderName(plan.getPlanName() + "-" + lineNo);
                    order.setQuantity(plan.getPlanNum());
                    order.setStartTime(plan.getStartTime().atStartOfDay());
                    order.setEndTime(plan.getEndTime().atStartOfDay());

                    orderMapper.addOrder(order);
                    log.info("联动下发：计划 {} 生成生产订单 {}（产线 {}）", plan.getPlanNo(), orderNo, lineNo);
                }
                break;
            }

            case PAUSE: {
                // 级联暂停所有 RUNNING 状态的订单及其工单
                // 使用直接 DB 更新方式，避免循环依赖和逐订单触发 syncPlanStatusFromOrders 导致状态回写
                List<ProductionOrder> runningOrders = orderMapper.getOrderByPlan(plan.getPlanNo())
                        .stream().filter(o -> o.getStatus() == StateEnum.RUNNING).toList();
                for (ProductionOrder o : runningOrders) {
                    // 1. 先级联暂停该订单下所有 RUNNING 的工单
                    List<WorkOrder> runningWorkOrders = workOrderMapper.getWorkOrdersByOrderNo(o.getOrderNo())
                            .stream().filter(w -> w.getStatus() == StateEnum.RUNNING).toList();
                    for (WorkOrder wo : runningWorkOrders) {
                        workOrderMapper.updateWorkOrderStatus(wo.getWorkOrderNo(), StateEnum.PAUSED);
                        StateContext woCtx = new StateContext(wo.getWorkOrderNo(), ActionEnum.PAUSE, userId);
                        auditService.record("WORK_ORDER", woCtx, StateEnum.RUNNING, StateEnum.PAUSED);
                        log.info("计划 {} 暂停联动：工单 {} 已暂停", plan.getPlanNo(), wo.getWorkOrderNo());
                    }
                    // 2. 再更新订单状态
                    orderMapper.updateOrderStatus(o.getOrderNo(), StateEnum.PAUSED);
                    StateContext orderCtx = new StateContext(o.getOrderNo(), ActionEnum.PAUSE, userId);
                    auditService.record("ORDER", orderCtx, StateEnum.RUNNING, StateEnum.PAUSED);
                    log.info("计划 {} 暂停联动：订单 {} 已暂停", plan.getPlanNo(), o.getOrderNo());
                }
                break;
            }

            case RESUME: {
                // 级联恢复所有 PAUSED 状态的订单及其工单
                List<ProductionOrder> pausedOrders = orderMapper.getOrderByPlan(plan.getPlanNo())
                        .stream().filter(o -> o.getStatus() == StateEnum.PAUSED).toList();
                for (ProductionOrder o : pausedOrders) {
                    // 1. 先级联恢复该订单下所有 PAUSED 的工单
                    List<WorkOrder> pausedWorkOrders = workOrderMapper.getWorkOrdersByOrderNo(o.getOrderNo())
                            .stream().filter(w -> w.getStatus() == StateEnum.PAUSED).toList();
                    for (WorkOrder wo : pausedWorkOrders) {
                        workOrderMapper.updateWorkOrderStatus(wo.getWorkOrderNo(), StateEnum.RUNNING);
                        StateContext woCtx = new StateContext(wo.getWorkOrderNo(), ActionEnum.RESUME, userId);
                        auditService.record("WORK_ORDER", woCtx, StateEnum.PAUSED, StateEnum.RUNNING);
                        log.info("计划 {} 恢复联动：工单 {} 已恢复", plan.getPlanNo(), wo.getWorkOrderNo());
                    }
                    // 2. 再恢复订单状态
                    orderMapper.updateOrderStatus(o.getOrderNo(), StateEnum.RUNNING);
                    StateContext orderCtx = new StateContext(o.getOrderNo(), ActionEnum.RESUME, userId);
                    auditService.record("ORDER", orderCtx, StateEnum.PAUSED, StateEnum.RUNNING);
                    log.info("计划 {} 恢复联动：订单 {} 已恢复执行", plan.getPlanNo(), o.getOrderNo());
                }
                break;
            }

            case CANCEL_PUBLISH: {
                // 级联作废所有非终态的订单及其工单
                List<ProductionOrder> orders = orderMapper.getOrderByPlan(plan.getPlanNo());
                for (ProductionOrder o : orders) {
                    StateEnum orderStatus = o.getStatus();
                    if (orderStatus != StateEnum.COMPLETED && orderStatus != StateEnum.TERMINATED) {
                        // 1. 先级联终止该订单下所有非终态的工单
                        List<WorkOrder> workOrders = workOrderMapper.getWorkOrdersByOrderNo(o.getOrderNo());
                        for (WorkOrder wo : workOrders) {
                            StateEnum woStatus = wo.getStatus();
                            if (woStatus != StateEnum.COMPLETED && woStatus != StateEnum.TERMINATED) {
                                workOrderMapper.updateWorkOrderStatus(wo.getWorkOrderNo(), StateEnum.TERMINATED);
                                StateContext woCtx = new StateContext(wo.getWorkOrderNo(), ActionEnum.TERMINATE, userId);
                                auditService.record("WORK_ORDER", woCtx, woStatus, StateEnum.TERMINATED);
                                log.info("计划 {} 取消发布联动：工单 {} 已作废", plan.getPlanNo(), wo.getWorkOrderNo());
                            }
                        }
                        // 2. 再终止订单（审计 action 使用 TERMINATE，语义准确）
                        orderMapper.updateOrderStatus(o.getOrderNo(), StateEnum.TERMINATED);
                        StateContext orderCtx = new StateContext(o.getOrderNo(), ActionEnum.TERMINATE, userId);
                        auditService.record("ORDER", orderCtx, orderStatus, StateEnum.TERMINATED);
                        log.info("计划 {} 取消发布联动：订单 {} 已作废", plan.getPlanNo(), o.getOrderNo());
                    }
                }
                break;
            }

            case TERMINATE: {
                // 级联终止所有非终态的订单及其工单
                List<ProductionOrder> orders = orderMapper.getOrderByPlan(plan.getPlanNo());
                for (ProductionOrder o : orders) {
                    StateEnum orderStatus = o.getStatus();
                    if (orderStatus != StateEnum.COMPLETED && orderStatus != StateEnum.TERMINATED) {
                        // 1. 先终止工单
                        List<WorkOrder> workOrders = workOrderMapper.getWorkOrdersByOrderNo(o.getOrderNo());
                        for (WorkOrder wo : workOrders) {
                            StateEnum woStatus = wo.getStatus();
                            if (woStatus != StateEnum.COMPLETED && woStatus != StateEnum.TERMINATED) {
                                workOrderMapper.updateWorkOrderStatus(wo.getWorkOrderNo(), StateEnum.TERMINATED);
                                StateContext woCtx = new StateContext(wo.getWorkOrderNo(), ActionEnum.TERMINATE, userId);
                                auditService.record("WORK_ORDER", woCtx, woStatus, StateEnum.TERMINATED);
                                log.info("计划 {} 作废联动：工单 {} 已作废", plan.getPlanNo(), wo.getWorkOrderNo());
                            }
                        }
                        // 2. 再终止订单
                        orderMapper.updateOrderStatus(o.getOrderNo(), StateEnum.TERMINATED);
                        StateContext orderCtx = new StateContext(o.getOrderNo(), ActionEnum.TERMINATE, userId);
                        auditService.record("ORDER", orderCtx, orderStatus, StateEnum.TERMINATED);
                        log.info("计划 {} 作废联动：订单 {} 已作废", plan.getPlanNo(), o.getOrderNo());
                    }
                }
                break;
            }

            default:
                // START_WORK / FINISH_WORK 不触发向下联动
                break;
        }
    }

    /**
     * 查询指定工艺流程 ID 对应的可用产线编号
     * 返回第一条空闲产线，若无则返回 null
     */
    private String findLineByProcessFlowId(Integer processFlowId) {
        return lineMapper.getAvailableLineNoByFlowId(processFlowId);
    }

    /**
     * 由下层的 Order 状态变更触发，向上同步 Plan 的状态（Order → Plan 联动上传）
     * 聚合规则：
     *   - 任一 Order 处于 RUNNING → Plan = RUNNING
     *   - 所有 Order 处于 PAUSED/COMPLETED 且无 RUNNING → Plan = PAUSED
     *   - 所有 Order 处于 COMPLETED → Plan = COMPLETED
     *
     * @param planNo 计划编号
     * @param userId 触发此联动的操作人（来自 Order 的状态变更人）
     */
    @Transactional(rollbackFor = Exception.class)
    public void syncPlanStatusFromOrders(String planNo, Integer userId) {
        Plan plan = planMapper.getPlanByNo(planNo);
        if (plan == null) {
            log.warn("联动上传：计划 {} 不存在，跳过", planNo);
            return;
        }

        List<ProductionOrder> orders = orderMapper.getOrderByPlan(planNo);
        if (orders.isEmpty()) {
            log.info("联动上传：计划 {} 下无订单，跳过", planNo);
            return;
        }

        // 单次遍历对订单按状态归类计数，替代多轮 Stream 扫描（O(N) 替代 O(8N)）
        // 设计依据：RUNNING → COMPLETED → PAUSED 优先级递减
        int countRunning = 0, countPaused = 0, countCompleted = 0, countTerminated = 0, countPreExec = 0;
        int totalCompletedQty = 0;

        for (ProductionOrder o : orders) {
            StateEnum st = o.getStatus();
            if (st == StateEnum.RUNNING) {
                countRunning++;
            } else if (st == StateEnum.PAUSED) {
                countPaused++;
            } else if (st == StateEnum.COMPLETED) {
                countCompleted++;
                totalCompletedQty += (o.getQuantityProduced() != null ? o.getQuantityProduced() : 0);
            } else if (st == StateEnum.TERMINATED) {
                countTerminated++;
            } else {
                countPreExec++;  // CREATED / RELEASED
            }
        }

        StateEnum currentStatus = plan.getStatus();
        StateEnum newStatus = currentStatus;
        boolean anyRunning = countRunning > 0;
        boolean allTerminal = countRunning == 0 && countPaused == 0 && countPreExec == 0;
        boolean hasCompleted = countCompleted > 0;
        boolean noneRunning = countRunning == 0;
        boolean hasExecutedOrders = (countRunning + countPaused + countCompleted + countTerminated) > 0;

        // RUNNING 判定：任一订单处于 RUNNING → Plan = RUNNING（最高优先级）
        if (anyRunning && currentStatus != StateEnum.RUNNING) {
            newStatus = StateEnum.RUNNING;
        }
        // COMPLETED 判定：所有订单处于终态（COMPLETED / TERMINATED），且至少一个 COMPLETED
        else if (allTerminal && hasCompleted && currentStatus != StateEnum.COMPLETED) {
            newStatus = StateEnum.COMPLETED;
            planMapper.updatePlanCompletedNum(planNo, totalCompletedQty);
            log.info("联动上传：计划 {} 已完成，汇总产量={}", planNo, totalCompletedQty);
        }
        // PAUSED 判定：所有已执行订单暂停或终态，至少一个暂停，无运行中订单
        else if (hasExecutedOrders && noneRunning && countPaused > 0 && currentStatus == StateEnum.RUNNING) {
            newStatus = StateEnum.PAUSED;
        }

        if (newStatus != currentStatus) {
            planMapper.updatePlanStatus(planNo, newStatus);
            StateContext context = new StateContext(planNo, null, userId);
            auditService.record("PLAN", context, currentStatus, newStatus);
            log.info("联动上传：计划 {} 从 [{}] 自动变更为 [{}]", planNo,
                    currentStatus.getDesc(), newStatus.getDesc());
        }
    }
}
