package com.xtax.enums;

import lombok.Getter;

@Getter
public enum StateEnum {

    CREATED("CREATED", "创建"),

    RELEASED("RELEASED", "发布"),

    RUNNING("RUNNING", "执行"),

    PAUSED("PAUSED", "暂停"),

    COMPLETED("COMPLETED", "完成");

    private final String code;
    private final String desc;

    StateEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public StateEnum next(ActionEnum action) {
        switch (this) {
            case CREATED:
                if (action == ActionEnum.PUBLISH) {
                    return RELEASED;
                }
                break;
            case RELEASED:
                if (action == ActionEnum.CANCEL_PUBLISH) {
                    return CREATED;
                } else if (action == ActionEnum.START_WORK) {
                    return RUNNING;
                }
                break;
            case RUNNING:
                if (action == ActionEnum.PAUSE) {
                    return PAUSED;
                } else if (action == ActionEnum.FINISH_WORK) {
                    return COMPLETED;
                }
                break;
            case PAUSED:
                if (action == ActionEnum.RESUME) {
                    return RUNNING;
                }
                break;
            case COMPLETED:
                break;
        }
        throw new IllegalStateException("状态 " + this + " 无法执行动作 " + action);
    }
}