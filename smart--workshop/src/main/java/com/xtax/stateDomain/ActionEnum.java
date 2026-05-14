package com.xtax.stateDomain;

import lombok.Getter;

/**
 * 状态动作枚举
 * 说明：
 * - Controller 层只能接收 Action
 * - 不允许直接 set 状态
 */
@Getter
public enum ActionEnum {

    /* ========= 决策型动作（管理行为） ========= */

    /**
     * 发布
     * CREATED -> RELEASED
     */
    PUBLISH("PUBLISH", "发布"),


    /* ========= 干预型动作（人为介入） ========= */

    /**
     * 取消发布
     * RELEASED -> CREATED
     */
    CANCEL_PUBLISH("CANCEL_PUBLISH", "取消发布"),

    /**
     * 暂停
     * RUNNING -> PAUSED,暂停执行
     */
    PAUSE("PAUSE", "暂停"),

    /**
     * 恢复
     * PAUSED -> RUNNING
     */
    RESUME("RESUME", "恢复执行"),


    /* ========= 事实型动作（不可直接改状态） ========= */

    /**
     * 开始作业（事实）
     * 由现场行为触发
     */
    START_WORK("START_WORK", "开始作业"),

    /**
     * 完成作业（事实）
     */
    FINISH_WORK("FINISH_WORK", "完成作业");

    private final String code;
    private final String desc;

    ActionEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
