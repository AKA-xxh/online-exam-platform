package com.exam.common.enums;

import lombok.Getter;

/**
 * 考试考生状态枚举
 */
@Getter
public enum ExamStudentStatusEnum {
    NOT_STARTED(0, "未开始"),
    IN_PROGRESS(1, "考试中"),
    SUBMITTED(2, "已交卷"),
    GRADED(3, "已出分"),
    ABSENT(4, "缺考");

    private final int code;
    private final String desc;

    ExamStudentStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
