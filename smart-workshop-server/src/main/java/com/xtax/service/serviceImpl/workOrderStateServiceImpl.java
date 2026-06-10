package com.xtax.service.serviceImpl;

import com.xtax.audit.AuditService;
import com.xtax.entity.ProductionOrder;
import com.xtax.entity.WorkOrder;
import com.xtax.enums.ActionEnum;
import com.xtax.enums.StateEnum;
import com.xtax.exception.BusinessException;
import com.xtax.mapper.orderMapper;
import com.xtax.mapper.userMapper;
import com.xtax.mapper.workOrderMapper;
import com.xtax.plicy.workOrderPermissionPolicy;
import com.xtax.service.workOrderStateService;
import com.xtax.stateDomain.StateContext;
import com.xtax.stateDomain.workOrderStateMachine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 工单状态服务实现
 * 完整管道：权限校验 → 状态机校验 → 业务验证 → 持久化 → 审计 → 联动
 *
 * 工单层是最靠近现场的执行层：
 * - START_WORK / FINISH_WORK 由员工手动确认
 * - PAUSE / RESUME 由员工或主管操作
 * - TERMINATE 由主管操作
 * - PUBLISH / CANCEL_PUBLISH 由订单发布联动触发
 */
@Slf4j
@Service
public class workOrderStateServiceImpl implements workOrderStateService {
    @Autowired
    private workOrderStateMachine workOrderStateMachine;
    @Autowired
    private workOrderPermissionPolicy permissionPolicy;
    @Autowired
    private AuditService auditService;
    @Autowired
    private workOrderMapper workOrderMapper;
    @Autowired
    private orderMapper orderMapper;
    @Autowired
    private planStateServiceImpl planStateService;
    @Autowired
    private userMapper userMapper;

    /**
     * 统一的状态处理入口
     * 管道：权限校验 → 状态机校验 → 业务验证 → 持久化 → 审计 → 联动
     *
     * @param workOrderNo 工单编号
     * @param action      执行动作
     * @param userId      当前操作用户
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void handle(String workOrderNo, ActionEnum action, Integer userId) {
        // 1. 获取业务对象并构建上下文
        WorkOrder workOrder = workOrderMapper.getWorkOrderByNo(workOrderNo);
        if (workOrder == null) {
            throw new IllegalArgumentException("未找到编号为 " + workOrderNo + " 的工单");
        }

        StateContext context = new StateContext(workOrderNo, action, userId);
        StateEnum fromStatus = workOrder.getStatus();

        // 2. 权限校验（角色级别）
        permissionPolicy.check(userId, action);

        // 3. 状态机迁移合法性校验
        workOrderStateMachine.check(fromStatus, action);

        // 4. 员工级别附加校验（userId 匹配）
        checkEmployeePermission(workOrder, action, userId);

        // 5. TERMINATE 业务校验：仅 RELEASED 或 PAUSED 状态可作废
        if (ActionEnum.TERMINATE.equals(action)) {
            if (fromStatus != StateEnum.RELEASED && fromStatus != StateEnum.PAUSED) {
                throw new BusinessException("工单当前状态 [" + fromStatus.getDesc()
                        + "] 不允许作废，仅 RELEASED 或 PAUSED 状态可作废");
            }
        }

        // 6. 计算新状态并持久化（根据不同动作类型做差异化处理）
        StateEnum toStatus = fromStatus.next(action);

        switch (action) {
            case PUBLISH:
                // 由订单发布联动触发：变更状态为 RELEASED
                workOrderMapper.updateWorkOrderStatus(workOrderNo, toStatus);
                log.info("工单 {} 发布成功（联动触发）", workOrderNo);
                break;

            case CANCEL_PUBLISH:
                // 由订单取消发布联动触发：回退到 CREATED
                workOrderMapper.updateWorkOrderStatus(workOrderNo, toStatus);
                log.info("工单 {} 取消发布（联动触发）", workOrderNo);
                break;

            case START_WORK:
                // 员工确认开始作业：锚定实际开始时间
                workOrderMapper.updateWorkOrderStatusAndStartTime(workOrderNo, toStatus);
                log.info("工单 {} 开始作业，员工 ID={}", workOrderNo, userId);
                break;

            case FINISH_WORK:
                // 员工确认完成作业：锚定实际结束时间
                workOrderMapper.updateWorkOrderStatusAndEndTime(workOrderNo, toStatus);
                log.info("工单 {} 完成作业，员工 ID={}", workOrderNo, userId);
                break;

            case PAUSE:
            case RESUME:
            case TERMINATE:
                // 人工干预：直接变更状态
                workOrderMapper.updateWorkOrderStatus(workOrderNo, toStatus);
                log.info("工单 {} 执行动作 [{}]，操作人 ID={}", workOrderNo, action.getDesc(), userId);
                break;

            default:
                workOrderMapper.updateWorkOrderStatus(workOrderNo, toStatus);
                break;
        }

        // 7. 审计记录
        auditService.record("WORK_ORDER", context, fromStatus, toStatus);

        // 8. 状态联动处理（向上联动：WorkOrder → Order → Plan）
        workOrder.setStatus(toStatus);
        handleLinkage(workOrder, action, userId);
    }

    /**
     * 员工级别附加权限校验
     * START_WORK / FINISH_WORK：必须为本工单的派工人员
     * PAUSE / RESUME：员工需 userId 匹配，主管可跨工单操作
     */
    private void checkEmployeePermission(WorkOrder workOrder, ActionEnum action, Integer userId) {
        switch (action) {
            case START_WORK:
            case FINISH_WORK:
                // 必须为本工单的派工人员
                if (!workOrder.getUserId().equals(userId)) {
                    throw new SecurityException("非本工单派工人员，无权限执行此操作");
                }
                break;

            case PAUSE:
            case RESUME:
                // 本工单派工人员，允许操作
                if (workOrder.getUserId().equals(userId)) {
                    break;
                }
                // 非本工单人员：必须是主管角色才允许跨工单操作
                String position = userMapper.getUserById(userId).getPosition();
                if (!"车间主任".equals(position) && !"生产主管".equals(position)) {
                    throw new SecurityException("非本工单派工人员且非主管，无权限暂停/恢复此工单");
                }
                break;

            default:
                // PUBLISH / CANCEL_PUBLISH / TERMINATE 不做员工归属校验
                break;
        }
    }

    /**
     * 工单状态变更的联动处理
     * 向上联动：WorkOrder → Order → Plan（同步订单及计划状态）
     *
     * 规则：
     * - 任一关键工单 START_WORK → 订单首次变为 RUNNING → 计划同步为 RUNNING
     * - 任一关键工单 PAUSE（且无其他 RUNNING 关键工单）→ 订单变为 PAUSED → 计划同步
     * - 任一关键工单 RESUME → 订单恢复为 RUNNING → 计划同步
     * - 所有工单 FINISH_WORK → 订单变为 COMPLETED（含产量汇总）→ 计划同步
     * - 所有工单 TERMINATE → 订单变为 TERMINATED → 计划同步
     *
     * @param workOrder 工单对象
     * @param action    执行的动作
     * @param userId    触发联动的操作人（透传至审计记录及计划同步）
     */
    @Override
    public void handleLinkage(WorkOrder workOrder, ActionEnum action, Integer userId) {
        String orderNo = workOrder.getOrderNo();
        if (orderNo == null) {
            return;
        }

        // 仅关键工单的状态变更才触发订单状态联动
        if (!Boolean.TRUE.equals(workOrder.getIsCritical())) {
            log.info("工单 {} 为非关键工单，不触发订单状态联动", workOrder.getWorkOrderNo());
            return;
        }

        ProductionOrder order = orderMapper.getOrderByNo(orderNo);
        if (order == null) {
            log.warn("联动上传：订单 {} 不存在，跳过", orderNo);
            return;
        }

        List<WorkOrder> allWorkOrders = workOrderMapper.getWorkOrdersByOrderNo(orderNo);
        List<WorkOrder> criticalWorkOrders = workOrderMapper.getCriticalWorkOrdersByOrder(orderNo);

        if (allWorkOrders.isEmpty()) {
            return;
        }

        StateEnum currentOrderStatus = order.getStatus();
        StateEnum newOrderStatus = currentOrderStatus;

        switch (action) {
            case START_WORK:
                // 首次有关键工单开始作业，订单进入 RUNNING
                if (currentOrderStatus != StateEnum.RUNNING) {
                    newOrderStatus = StateEnum.RUNNING;
                }
                break;

            case PAUSE:
                // 暂停时检查是否还有其它正在 RUNNING 的关键工单
                boolean hasRunningCritical = criticalWorkOrders.stream()
                        .anyMatch(w -> w.getStatus() == StateEnum.RUNNING
                                && !w.getWorkOrderNo().equals(workOrder.getWorkOrderNo()));
                if (!hasRunningCritical && currentOrderStatus == StateEnum.RUNNING) {
                    newOrderStatus = StateEnum.PAUSED;
                }
                break;

            case RESUME:
                // 从暂停恢复，订单恢复为 RUNNING
                if (currentOrderStatus == StateEnum.PAUSED) {
                    newOrderStatus = StateEnum.RUNNING;
                }
                break;

            case FINISH_WORK:
                // 所有工单都完成时，订单完成
                // 当前工单刚变为 COMPLETED，检查是否还有其他未完成的工单
                boolean anyNotCompleted = allWorkOrders.stream()
                        .anyMatch(w -> w.getStatus() != StateEnum.COMPLETED
                                && !w.getWorkOrderNo().equals(workOrder.getWorkOrderNo()));
                if (!anyNotCompleted && currentOrderStatus != StateEnum.COMPLETED) {
                    newOrderStatus = StateEnum.COMPLETED;
                    // 汇总工单产量到订单
                    int totalActual = allWorkOrders.stream()
                            .mapToInt(w -> w.getActualQuantity() != null ? w.getActualQuantity() : 0).sum();
                    int totalScrap = allWorkOrders.stream()
                            .mapToInt(w -> w.getScrapQuantity() != null ? w.getScrapQuantity() : 0).sum();
                    orderMapper.updateOrderQuantity(orderNo, totalActual,
                            totalActual - totalScrap, totalScrap);
                    log.info("联动上传：订单 {} 产量汇总 — 实际完成={}，合格品={}，次品={}",
                            orderNo, totalActual, totalActual - totalScrap, totalScrap);
                }
                break;

            case TERMINATE:
                // 所有工单均已作废时，订单同步作废
                boolean allTerminated = allWorkOrders.stream()
                        .allMatch(w -> w.getStatus() == StateEnum.TERMINATED
                                || w.getWorkOrderNo().equals(workOrder.getWorkOrderNo()));
                if (allTerminated && currentOrderStatus != StateEnum.TERMINATED
                        && currentOrderStatus != StateEnum.COMPLETED) {
                    newOrderStatus = StateEnum.TERMINATED;
                }
                break;

            default:
                // PUBLISH / CANCEL_PUBLISH 不触发订单状态联动
                break;
        }

        // 如果订单状态需要变更，执行更新
        if (newOrderStatus != currentOrderStatus) {
            orderMapper.updateOrderStatus(orderNo, newOrderStatus);
            StateContext orderContext = new StateContext(orderNo, action, userId);
            auditService.record("ORDER", orderContext, currentOrderStatus, newOrderStatus);
            log.info("联动上传：工单 {} 状态变更触发订单 {} 从 [{}] 变更为 [{}]",
                    workOrder.getWorkOrderNo(), orderNo,
                    currentOrderStatus.getDesc(), newOrderStatus.getDesc());

            // 向上联动：Order → Plan（同步计划状态）
            if (order.getPlanNo() != null) {
                planStateService.syncPlanStatusFromOrders(order.getPlanNo(), userId);
            }
        }
    }
}
