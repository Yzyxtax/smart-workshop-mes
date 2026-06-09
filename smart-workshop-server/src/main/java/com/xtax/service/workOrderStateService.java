package com.xtax.service;

import com.xtax.entity.WorkOrder;
import com.xtax.enums.ActionEnum;

/**
 * 工单状态服务接口
 * 负责工单状态变更的完整管道处理
 */
public interface workOrderStateService {

    /**
     * 统一的状态处理入口
     * 管道：权限校验 → 状态机校验 → 业务验证 → 持久化 → 审计 → 联动
     *
     * @param workOrderNo 工单编号
     * @param action      执行动作
     * @param userId      当前操作用户
     */
    void handle(String workOrderNo, ActionEnum action, Integer userId);

    /**
     * 工单状态变更后的联动处理
     * 向上联动：WorkOrder → Order（同步订单状态）
     *
     * @param workOrder 工单对象
     * @param action    执行的动作
     */
    void handleLinkage(WorkOrder workOrder, ActionEnum action);
}
