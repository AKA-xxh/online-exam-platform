package com.exam.common.result;

import lombok.Getter;

/**
 * 统一错误码枚举
 * <p>
 * 规范：5位数字，前3位为HTTP状态码大类，后2位为具体错误细分。
 * 例如 40001 = 400(参数错误) + 01(具体原因)
 */
@Getter
public enum ErrorCode {

    // ==================== 成功 ====================
    SUCCESS(200, "操作成功"),

    // ==================== 参数错误 400xx ====================
    PARAM_INVALID(40001, "请求参数不合法"),
    PARAM_MISSING(40002, "缺少必要参数"),
    PARAM_TYPE_ERROR(40003, "参数类型错误"),

    // ==================== 认证错误 401xx ====================
    UNAUTHORIZED(40101, "未登录或登录已过期"),
    TOKEN_EXPIRED(40102, "Token 已过期"),
    TOKEN_INVALID(40103, "Token 无效"),
    LOGIN_FAILED(40104, "用户名或密码错误"),
    ACCOUNT_DISABLED(40105, "账号已被禁用"),
    CAPTCHA_ERROR(40106, "验证码错误"),

    // ==================== 权限错误 403xx ====================
    FORBIDDEN(40301, "权限不足"),
    NO_PERMISSION(40302, "无此操作权限"),

    // ==================== 资源不存在 404xx ====================
    NOT_FOUND(40401, "资源不存在"),
    USER_NOT_FOUND(40402, "用户不存在"),
    COURSE_NOT_FOUND(40403, "课程不存在"),
    QUESTION_NOT_FOUND(40404, "题目不存在"),
    PAPER_NOT_FOUND(40405, "试卷不存在"),
    EXAM_NOT_FOUND(40406, "考试不存在"),

    // ==================== 业务冲突 409xx ====================
    PHONE_EXISTS(40901, "手机号已被注册"),
    EMAIL_EXISTS(40902, "邮箱已被注册"),
    EXAM_ALREADY_SUBMITTED(40903, "考试已交卷，不能重复提交"),
    EXAM_NOT_STARTED(40904, "考试尚未开始"),
    EXAM_ENDED(40905, "考试已结束"),
    PAPER_IN_USE(40906, "试卷正在被考试使用，无法修改"),

    // ==================== 限流 429xx ====================
    RATE_LIMIT(42901, "请求过于频繁，请稍后再试"),

    // ==================== 服务端错误 500xx ====================
    INTERNAL_ERROR(50001, "服务器内部错误"),
    DB_ERROR(50002, "数据库操作失败"),
    FILE_UPLOAD_ERROR(50003, "文件上传失败"),
    FILE_TOO_LARGE(50004, "文件大小超出限制"),

    // ==================== 考试专用 ====================
    EXAM_CHEAT_DETECTED(60001, "检测到异常行为，考试已终止"),
    EXAM_SCREEN_SWITCH_LIMIT(60002, "切屏次数已达上限"),
    EXAM_TIME_UP(60003, "考试时间已到，系统自动交卷");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
