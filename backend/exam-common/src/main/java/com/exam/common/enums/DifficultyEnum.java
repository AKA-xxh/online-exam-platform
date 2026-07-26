package com.exam.common.enums;

import lombok.Getter;

/**
 * 难度枚举
 */
@Getter
public enum DifficultyEnum {
    EASY(1, "简单"),
    MEDIUM(2, "中等"),
    HARD(3, "困难");

    private final int code;
    private final String desc;

    DifficultyEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
