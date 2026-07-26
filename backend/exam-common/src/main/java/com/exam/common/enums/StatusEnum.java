package com.exam.common.enums;

import lombok.Getter;

/**
 * 通用状态枚举
 */
@Getter
public enum StatusEnum {
    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    private final int code;
    private final String desc;

    StatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
