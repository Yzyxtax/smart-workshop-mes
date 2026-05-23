package com.xtax.stateDomain;

import com.xtax.enums.ActionEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * 状态变更操作的上下文对象。
 * 封装了一次状态变更所需的所有关键信息，用于在服务层、策略层和审计层之间传递。
 */
@Setter
@Getter
public class StateContext {

    /**
     * 业务对象的唯一标识符 (bizNo)。
     * 例如：PlanNo, OrderNo, WorkOrderNo。
     */
    private String bizNo;

    /**
     * 用户执行的动作 (action)。
     * 例如：PUBLISH, PAUSE, START_WORK 等。
     */
    private ActionEnum action;

    /**
     * 执行该操作的操作员 (userId)。
     */
    private Integer userId;

    // 构造函数

    /**
     * 全参数构造函数。
     *
     * @param bizNo    业务对象ID
     * @param action   执行的动作
     * @param userId 操作员
     */
    public StateContext(String bizNo, ActionEnum action, Integer userId) {
        this.bizNo = bizNo;
        this.action = action;
        this.userId = userId;
    }

    // 重写 equals 和 hashCode (当需要将 StateContext 作为 Map 的 key 或放入 Set 时)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StateContext that = (StateContext) o;
        return Objects.equals(bizNo, that.bizNo) &&
                action == that.action &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bizNo, action, userId);
    }

    // 重写 toString (便于调试和日志输出)
    @Override
    public String toString() {
        return "StateContext{" +
                "bizNo=" + bizNo +
                ", action=" + action +
                ", userId=" + userId +
                '}';
    }
}