package com.xtax.stateDomain;

import com.xtax.enums.ActionEnum;
import com.xtax.enums.StateEnum;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * 生产订单状态机
 * 职责：
 *  1. 定义 Order 的状态迁移规则
 *  2. 校验某个 Action 在当前状态下是否合法
 * 注意：
 *  - 这里只做“状态规则校验”
 *  - 不涉及权限、不涉及业务数据、不查数据库
 */
@Component
public class orderStateMachine {
    /**
     * 状态 -> 允许的动作集合
     */
    private static final Map<StateEnum, Set<ActionEnum>> STATE_ACTION_MAP = new EnumMap<>(StateEnum.class);

    static {
        // CREATED：草稿态
        STATE_ACTION_MAP.put(
                StateEnum.CREATED,
                EnumSet.of(
                        ActionEnum.PUBLISH
                )
        );

        // RELEASED：已发布（冻结）
        // START_WORK 由工单事实驱动，TERMINATE 为人工作废
        STATE_ACTION_MAP.put(
                StateEnum.RELEASED,
                EnumSet.of(
                        ActionEnum.CANCEL_PUBLISH,
                        ActionEnum.START_WORK,
                        ActionEnum.TERMINATE
                )
        );

        // RUNNING：执行中
        // PAUSE 为人工干预，FINISH_WORK 由工单事实驱动
        STATE_ACTION_MAP.put(
                StateEnum.RUNNING,
                EnumSet.of(
                        ActionEnum.PAUSE,
                        ActionEnum.FINISH_WORK
                )
        );

        // PAUSED：已暂停
        STATE_ACTION_MAP.put(
                StateEnum.PAUSED,
                EnumSet.of(
                        ActionEnum.RESUME
                )
        );

        // COMPLETED：已完成（终态）
        STATE_ACTION_MAP.put(
                StateEnum.COMPLETED,
                EnumSet.noneOf(ActionEnum.class)
        );

        // TERMINATED：已作废（终态）
        STATE_ACTION_MAP.put(
                StateEnum.TERMINATED,
                EnumSet.noneOf(ActionEnum.class)
        );
    }

    /**
     * 校验状态动作是否合法
     *
     * @param currentState 当前状态
     * @param action       执行动作
     */
    public void check(StateEnum currentState, ActionEnum action) {

        Set<ActionEnum> allowedActions = STATE_ACTION_MAP.get(currentState);

        if (allowedActions == null || !allowedActions.contains(action)) {
            throw new IllegalStateException(
                    String.format(
                            "Order 状态 [%s] 不允许执行动作 [%s]",
                            currentState,
                            action
                    )
            );
        }
    }

    /**
     * 判断是否允许（不抛异常，供部分场景使用）
     */
    public boolean isAllowed(StateEnum currentState, ActionEnum action) {
        Set<ActionEnum> allowedActions = STATE_ACTION_MAP.get(currentState);
        return allowedActions != null && allowedActions.contains(action);
    }
}
