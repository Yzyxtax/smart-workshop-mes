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
}