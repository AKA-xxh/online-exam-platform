package com.exam.common.exception;

import com.exam.common.result.ErrorCode;
import lombok.Getter;

/**
 * 业务异常
 * <p>
 * 业务逻辑中遇到不符合预期的情况时抛出此异常。
 * 由 GlobalExceptionHandler 统一捕获并转换为标准错误响应。
 * <p>
 * 使用示例：
 * throw new BusinessException(ErrorCode.EXAM_ENDED);           // 只传错误码
 * throw new BusinessException(ErrorCode.PARAM_INVALID, "标题不能为空");  // 覆盖错误信息
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        super(message);
        this.code = ErrorCode.INTERNAL_ERROR.getCode();
    }
}
