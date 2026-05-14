package com.xtax.service.serviceImpl;

import com.xtax.audit.AuditService;
import com.xtax.mapper.planMapper;
import com.xtax.plicy.GatePolicy;
import com.xtax.plicy.planPermissionPolicy;
import com.xtax.pojo.Plan;
import com.xtax.service.planStateService;
import com.xtax.stateDomain.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // 5. 计算并更新新状态
        // 利用 StateEnum 中定义的 switch 逻辑获取 next 状态
        StateEnum toStatus = fromStatus.next(action);
        plan.setStatus(toStatus);
        planMapper.updatePlanStatus(planNo, toStatus);

        // 6. 异步/同步审计记录 (AuditService)
        // 记录：谁在什么时间，通过什么动作，将 Plan 从 A 状态变更为 B 状态
        auditService.record("PLAN",context, fromStatus, toStatus);

        // 7. 状态联动处理
        handleLinkage(plan, action);
    }

    /**
     * 处理状态联动逻辑
     */
    public void handleLinkage(Plan plan, ActionEnum action) {
        if (ActionEnum.PUBLISH.equals(action)) {
            // TODO: 调用订单服务，根据计划拆分并生成生产订单 (Order)
        }
    }
}
