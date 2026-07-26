package com.exam.common.enums;

import lombok.Getter;

/**
 * 用户类型枚举
 */
@Getter
public enum UserTypeEnum {
    STUDENT(1, "学员"),
    TEACHER(2, "教师"),
    ADMIN(3, "管理员");

    private final int code;
    private final String desc;

    UserTypeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
