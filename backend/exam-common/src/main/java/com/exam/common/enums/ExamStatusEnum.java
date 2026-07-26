package com.exam.common.enums;

import lombok.Getter;

/**
 * 考试状态枚举
 */
@Getter
public enum ExamStatusEnum {
    NOT_STARTED(0, "未开始"),
    IN_PROGRESS(1, "进行中"),
    FINISHED(2, "已结束"),
    CANCELLED(3, "已取消");

    private final int code;
    private final String desc;

    ExamStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
