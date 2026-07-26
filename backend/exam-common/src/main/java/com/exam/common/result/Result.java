package com.exam.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一响应体
 * <p>
 * 所有 API 接口返回此格式，前端统一解析处理。
 * 成功示例：{"code":200, "message":"操作成功", "data":{...}, "timestamp":...}
 * 失败示例：{"code":40001, "message":"参数错误", "data":null, "timestamp":...}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态码（200=成功，其他=错误码） */
    private int code;

    /** 提示信息 */
    private String message;

    /** 返回数据 */
    private T data;

    /** 时间戳 */
    private long timestamp;

    // ==================== 成功响应 ====================

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "操作成功", data, System.currentTimeMillis());
    }

    public static <T> Result<T> ok(String message, T data) {
        return new Result<>(200, message, data, System.currentTimeMillis());
    }

    // ==================== 失败响应 ====================

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null, System.currentTimeMillis());
    }

    public static <T> Result<T> fail(ErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage(), null, System.currentTimeMillis());
    }

    public static <T> Result<T> fail(ErrorCode errorCode, String message) {
        return new Result<>(errorCode.getCode(), message, null, System.currentTimeMillis());
    }
}
