package com.xtax.stateDomain;

import lombok.Getter;

/**
 * 统一状态枚举
 * 说明：
 * - 三层对象（Plan / Order / WorkOrder）共用
 * - 语义由具体业务层解释
 */
@Getter
public enum StateEnum {

    /**
     * 创建（草稿态）
     */
    CREATED("CREATED", "创建"),

    /**
     * 已发布（冻结，可执行）
     */
    RELEASED("RELEASED", "发布"),

    /**
     * 执行中
     */
    RUNNING("RUNNING", "执行"),

    /**
     * 已暂停
     */
    PAUSED("PAUSED", "暂停"),

    /**
     * 已完成
     */
    COMPLETED("COMPLETED", "完成");

    private final String code;
    private final String desc;

    StateEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据动作获取下一个状态
     */
    public StateEnum next(ActionEnum action) {
        switch (this) {
            case CREATED:
                if (ActionEnum.PUBLISH.equals(action)) {
                    return RELEASED;
                }
                break;
            case RELEASED:
                if (ActionEnum.CANCEL_PUBLISH.equals(action)){
                    return CREATED;
                }
                if (ActionEnum.START_WORK.equals(action)) {
                    return RUNNING;
                }
                break;
            case RUNNING:
                if (ActionEnum.PAUSE.equals(action)) {
                    return PAUSED;
                }
                if (ActionEnum.FINISH_WORK.equals(action)) {
                    return COMPLETED;
                }
                break;
            case PAUSED:
                if (ActionEnum.RESUME.equals(action)) {
                    return RUNNING;
                }
                break;
        }
        throw new IllegalArgumentException("状态 " + this + " 不允许执行动作 " + action);
    }
}
