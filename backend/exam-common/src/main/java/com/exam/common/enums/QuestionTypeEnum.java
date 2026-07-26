package com.exam.common.enums;

import lombok.Getter;

/**
 * 题目类型枚举
 */
@Getter
public enum QuestionTypeEnum {
    SINGLE_CHOICE(1, "单选题"),
    MULTI_CHOICE(2, "多选题"),
    TRUE_FALSE(3, "判断题"),
    ESSAY(4, "简答题");

    private final int code;
    private final String desc;

    QuestionTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static QuestionTypeEnum fromCode(int code) {
        for (QuestionTypeEnum type : values()) {
            if (type.code == code) return type;
        }
        throw new IllegalArgumentException("未知题型: " + code);
    }

    /**
     * 是否为客观题（可自动判分）
     */
    public boolean isObjective() {
        return this != ESSAY;
    }
}
