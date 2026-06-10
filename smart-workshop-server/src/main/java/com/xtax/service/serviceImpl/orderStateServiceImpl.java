package com.xtax.service.serviceImpl;

import com.xtax.audit.AuditService;
import com.xtax.entity.ProductionLine;
import com.xtax.entity.ProductionOrder;
import com.xtax.entity.WorkOrder;
import com.xtax.enums.ActionEnum;
import com.xtax.enums.StateEnum;
import com.xtax.exception.BusinessException;
import com.xtax.mapper.lineMapper;
import com.xtax.mapper.orderMapper;
import com.xtax.mapper.processFlowMapper;
import com.xtax.mapper.processMapper;
import com.xtax.mapper.userMapper;
import com.xtax.plicy.GatePolicy;
import com.xtax.plicy.orderPermissionPolicy;
import com.xtax.service.orderStateService;
import com.xtax.service.workOrderStateService;
import com.xtax.stateDomain.StateContext;
import com.xtax.stateDomain.orderStateMachine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 订单状态服务实现
 * 完整管道：权限校验 → 状态机校验 → 门禁校验 → 业务验证 → 持久化 → 审计 → 联动
 */
@Slf4j
@Service
public class orderStateServiceImpl implements orderStateService {
    @Autowired
    private orderStateMachine orderStateMachine;
    @Autowired
    private orderPermissionPolicy permissionPolicy;
    @Autowired
    private GatePolicy gatePolicy;
    @Autowired
    private AuditService auditService;
    @Autowired
    private orderMapper orderMapper;
    @Autowired
    private planStateServiceImpl planStateService;
    @Autowired
    private processFlowMapper processFlowMapper;
    @Autowired
    private processMapper processMapper;
    @Autowired
    private userMapper userMapper;
    @Autowired
    private lineMapper lineMapper;
    @Autowired
    private workOrderStateService workOrderStateService;

    /**
     * 统一的状态处理入口
     * 管道：权限校验 → 状态机校验 → 门禁校验 → 业务验证 → 持久化 → 审计 → 联动
     *
     * @param orderNo 订单编号
     * @param action  执行动作
     * @param userId  当前操作用户
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void handle(String orderNo, ActionEnum action, Integer userId) {
        // 1. 获取业务对象并构建上下文
        ProductionOrder order = orderMapper.getOrderByNo(orderNo);
        if (order == null) {
            throw new IllegalArgumentException("未找到编号为 " + orderNo + " 的生产订单");
        }

        StateContext context = new StateContext(orderNo, action, userId);
        StateEnum fromStatus = order.getStatus();

        // 2. 权限校验
        permissionPolicy.check(userId, action);

        // 3. 状态机迁移合法性校验
        orderStateMachine.check(fromStatus, action);

        // 4. 门禁校验 — 仅在发布（PUBLISH）时触发
        if (ActionEnum.PUBLISH.equals(action)) {
            gatePolicy.checkOrder(context, order.getPlanNo(), order.getLineNo());
        }

        // 5. 业务验证 — 作废前校验无执行中工单
        if (ActionEnum.TERMINATE.equals(action)) {
            List<WorkOrder> workOrders = orderMapper.getWorkOrdersByOrderNo(orderNo);
            boolean anyRunning = workOrders.stream()
                    .anyMatch(w -> w.getStatus() == StateEnum.RUNNING);
            if (anyRunning) {
                throw new BusinessException("订单下存在执行中的工单，不允许作废");
            }
        }

        // 6. 计算新状态并持久化（根据不同动作类型做差异化处理）
        StateEnum toStatus = fromStatus.next(action);

        switch (action) {
            case PUBLISH:
                // 发布：变更状态，锁定产线
                orderMapper.updateOrderStatus(orderNo, toStatus);
                log.info("订单 {} 发布成功，产线 {} 已锁定", orderNo, order.getLineNo());
                break;

            case CANCEL_PUBLISH:
                // 取消发布：回退到 CREATED，释放产线
                orderMapper.updateOrderStatus(orderNo, toStatus);
                log.info("订单 {} 取消发布，产线 {} 已释放", orderNo, order.getLineNo());
                break;

            case START_WORK:
                // 工单事实驱动：开始作业，锚定实际开始时间
                orderMapper.updateOrderStatusAndStartTime(orderNo, toStatus);
                log.info("订单 {} 开始作业", orderNo);
                break;

            case FINISH_WORK:
                // 工单事实驱动：完成作业，锚定实际结束时间
                orderMapper.updateOrderStatusAndEndTime(orderNo, toStatus);
                log.info("订单 {} 完成作业", orderNo);
                break;

            case PAUSE:
            case RESUME:
            case TERMINATE:
                // 人工干预：直接变更状态
                orderMapper.updateOrderStatus(orderNo, toStatus);
                log.info("订单 {} 执行人工干预动作 [{}]", orderNo, action.getDesc());
                break;

            default:
                orderMapper.updateOrderStatus(orderNo, toStatus);
                break;
        }

        // 7. 审计记录
        auditService.record("ORDER", context, fromStatus, toStatus);

        // 8. 状态联动处理
        handleLinkage(order, action, userId);
    }

    /**
     * 订单状态变更的联动处理
     * 向上联动：Order → Plan（同步计划状态）
     * 向下联动：PUBLISH 时为产线各工序生成工单并自动发布
     *
     * @param order  订单对象
     * @param action 执行的动作
     * @param userId 触发联动的操作人（透传至计划同步及工单发布）
     */
    @Override
    public void handleLinkage(ProductionOrder order, ActionEnum action, Integer userId) {
        // 向上联动：同步计划状态（Order → Plan）
        if (order.getPlanNo() != null) {
            planStateService.syncPlanStatusFromOrders(order.getPlanNo(), userId);
        }

        // 向下联动：发布时根据工艺流程生成工单并自动发布
        if (ActionEnum.PUBLISH.equals(action)) {
            generateWorkOrders(order, userId);
        }

        // 向下联动：取消发布时作废所有非终态的工单
        if (ActionEnum.CANCEL_PUBLISH.equals(action)) {
            cancelWorkOrders(order, userId);
        }

        // 向下联动：作废时终止所有非终态的工单
        if (ActionEnum.TERMINATE.equals(action)) {
            terminateWorkOrders(order, userId);
        }

        // 向下联动：暂停时级联暂停所有 RUNNING 的工单
        if (ActionEnum.PAUSE.equals(action)) {
            pauseWorkOrders(order, userId);
        }

        // 向下联动：恢复时级联恢复所有 PAUSED 的工单
        if (ActionEnum.RESUME.equals(action)) {
            resumeWorkOrders(order, userId);
        }
    }

    /**
     * 订单发布（PUBLISH）时，根据产线绑定的工艺流程生成工单并自动发布
     * 1. 获取产线绑定的工艺流程
     * 2. 遍历每条工序
     * 3. 为每道工序查找具备技能的人员
     * 4. 生成工单（CREATED），编号格式为 {orderNo}-{processId}-{seq}
     * 5. 自动发布工单（CREATED → RELEASED）
     *
     * @param order  订单对象
     * @param userId 触发此操作的用户（透传至工单审计记录）
     */
    private void generateWorkOrders(ProductionOrder order, Integer userId) {
        // 获取产线绑定的工艺流程 ID
        ProductionLine line = lineMapper.getLine(order.getLineNo());
        if (line == null || line.getFlowId() == null) {
            log.warn("订单 {} 发布：产线 {} 未绑定工艺流程，跳过工单生成", order.getOrderNo(), order.getLineNo());
            return;
        }

        Integer flowId = line.getFlowId();
        List<Integer> processIdList = processFlowMapper.getProcessIdsByFlowId(flowId);
        if (processIdList == null || processIdList.isEmpty()) {
            log.warn("订单 {} 发布：工艺流程 ID={} 无工序定义，跳过工单生成", order.getOrderNo(), flowId);
            return;
        }

        int seq = 0;
        for (Integer processId : processIdList) {
            seq++;
            String processName = processMapper.getProcessById(processId);
            if (processName == null) {
                log.warn("订单 {} 发布：工序 ID={} 不存在，跳过", order.getOrderNo(), processId);
                continue;
            }

            // 查找具备该工序技能的可用人员
            List<Integer> skilledUserIds = userMapper.findUserIdsBySkill(processName);
            if (skilledUserIds == null || skilledUserIds.isEmpty()) {
                log.warn("订单 {} 发布：工序「{}」无可用人员，跳过工单生成", order.getOrderNo(), processName);
                continue;
            }
            Integer skilledUserId = skilledUserIds.get(0);

            // 生成工单编号：{orderNo}-{processId}-{seq}
            String workOrderNo = order.getOrderNo() + "-" + processId + "-" + seq;

            // 构建工单对象（初始状态 CREATED，后续由 PUBLISH 动作变更为 RELEASED）
            WorkOrder wo = new WorkOrder();
            wo.setWorkOrderNo(workOrderNo);
            wo.setOrderNo(order.getOrderNo());
            wo.setProcessId(processId);
            wo.setUserId(skilledUserId);
            // 首个工单为关键工单，影响订单状态聚合
            wo.setIsCritical(seq == 1);
            wo.setPlannedQuantity(order.getQuantity());
            wo.setActualQuantity(0);
            wo.setScrapQuantity(0);
            wo.setStatus(StateEnum.CREATED);
            wo.setStartTime(order.getStartTime());
            wo.setEndTime(order.getEndTime());

            // 写入工单
            orderMapper.insertWorkOrder(wo);
            log.info("订单 {} 发布联动：生成工单 {}（工序「{}」，派工人员 ID={}，关键={}）",
                    order.getOrderNo(), workOrderNo, processName, skilledUserId, wo.getIsCritical());

            // 自动发布工单（CREATED → RELEASED），系统联动触发
            workOrderStateService.handle(workOrderNo, ActionEnum.PUBLISH, userId);
            log.info("订单 {} 发布联动：工单 {} 已自动发布", order.getOrderNo(), workOrderNo);
        }
    }

    /**
     * 订单取消发布（CANCEL_PUBLISH）时，联动作废所有非终态的工单
     *
     * @param order  订单对象
     * @param userId 触发此操作的用户（透传至工单审计记录）
     */
    private void cancelWorkOrders(ProductionOrder order, Integer userId) {
        List<WorkOrder> workOrders = orderMapper.getWorkOrdersByOrderNo(order.getOrderNo());
        if (workOrders == null || workOrders.isEmpty()) {
            log.info("订单 {} 取消发布：无下属工单，跳过", order.getOrderNo());
            return;
        }

        int cancelCount = 0;
        for (WorkOrder wo : workOrders) {
            StateEnum woStatus = wo.getStatus();
            // 处理所有非终态的工单（CREATED / RELEASED / PAUSED）
            if (woStatus != StateEnum.COMPLETED && woStatus != StateEnum.TERMINATED) {
                orderMapper.updateWorkOrderStatus(wo.getWorkOrderNo(), StateEnum.TERMINATED);
                cancelCount++;
                log.info("订单 {} 取消发布联动：工单 {} 已作废（原状态: {}）", order.getOrderNo(), wo.getWorkOrderNo(), woStatus.getDesc());
            }
        }
        log.info("订单 {} 取消发布联动：共作废 {} 个工单", order.getOrderNo(), cancelCount);
    }

    /**
     * 订单作废（TERMINATE）时，联动作废所有非终态的工单
     *
     * @param order  订单对象
     * @param userId 触发此操作的用户
     */
    private void terminateWorkOrders(ProductionOrder order, Integer userId) {
        List<WorkOrder> workOrders = orderMapper.getWorkOrdersByOrderNo(order.getOrderNo());
        if (workOrders == null || workOrders.isEmpty()) {
            log.info("订单 {} 作废：无下属工单，跳过", order.getOrderNo());
            return;
        }

        int terminateCount = 0;
        for (WorkOrder wo : workOrders) {
            StateEnum woStatus = wo.getStatus();
            if (woStatus != StateEnum.COMPLETED && woStatus != StateEnum.TERMINATED) {
                orderMapper.updateWorkOrderStatus(wo.getWorkOrderNo(), StateEnum.TERMINATED);
                terminateCount++;
                log.info("订单 {} 作废联动：工单 {} 已作废", order.getOrderNo(), wo.getWorkOrderNo());
            }
        }
        log.info("订单 {} 作废联动：共作废 {} 个工单", order.getOrderNo(), terminateCount);
    }

    /**
     * 订单暂停（PAUSE）时，级联暂停所有 RUNNING 状态的工单
     *
     * @param order  订单对象
     * @param userId 触发此操作的用户
     */
    private void pauseWorkOrders(ProductionOrder order, Integer userId) {
        List<WorkOrder> workOrders = orderMapper.getWorkOrdersByOrderNo(order.getOrderNo());
        if (workOrders == null || workOrders.isEmpty()) {
            return;
        }

        for (WorkOrder wo : workOrders) {
            if (wo.getStatus() == StateEnum.RUNNING) {
                orderMapper.updateWorkOrderStatus(wo.getWorkOrderNo(), StateEnum.PAUSED);
                log.info("订单 {} 暂停联动：工单 {} 已暂停", order.getOrderNo(), wo.getWorkOrderNo());
            }
        }
    }

    /**
     * 订单恢复（RESUME）时，级联恢复所有 PAUSED 状态的工单
     *
     * @param order  订单对象
     * @param userId 触发此操作的用户
     */
    private void resumeWorkOrders(ProductionOrder order, Integer userId) {
        List<WorkOrder> workOrders = orderMapper.getWorkOrdersByOrderNo(order.getOrderNo());
        if (workOrders == null || workOrders.isEmpty()) {
            return;
        }

        for (WorkOrder wo : workOrders) {
            if (wo.getStatus() == StateEnum.PAUSED) {
                orderMapper.updateWorkOrderStatus(wo.getWorkOrderNo(), StateEnum.RUNNING);
                log.info("订单 {} 恢复联动：工单 {} 已恢复", order.getOrderNo(), wo.getWorkOrderNo());
            }
        }
    }
}
