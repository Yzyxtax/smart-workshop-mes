package com.xtax.enums;

import lombok.Getter;

@Getter
public enum ActionEnum {

    PUBLISH("PUBLISH", "发布"),

    CANCEL_PUBLISH("CANCEL_PUBLISH", "取消发布"),

    PAUSE("PAUSE", "暂停"),

    RESUME("RESUME", "恢复执行"),

    START_WORK("START_WORK", "开始作业"),

    FINISH_WORK("FINISH_WORK", "完成作业"),

    TERMINATE("TERMINATE", "作废");

    private final String code;
    private final String desc;

    ActionEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}