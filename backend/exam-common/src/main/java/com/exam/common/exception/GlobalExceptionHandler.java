package com.exam.common.exception;

import com.exam.common.result.ErrorCode;
import com.exam.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>
 * 统一处理所有未捕获的异常，将其转换为标准 Result 格式返回。
 * 这样前端只需要处理一种返回格式，不需要关心后端内部错误。
 * <p>
 * 类比：就像一个"前台接待员"，不管内部出了什么问题（参数不对、没权限、服务器炸了），
 *       都会用统一的语言（Result格式）告诉前端发生了什么。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== 业务异常 ====================

    /**
     * 处理业务异常 — 这是我们自己在逻辑中主动抛出的异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常 - {} {} -> code={}, message={}",
                request.getMethod(), request.getRequestURI(), e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    // ==================== 参数校验异常 ====================

    /**
     * 处理 @Valid/@Validated 校验失败（请求体参数校验）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败 - {}", msg);
        return Result.fail(ErrorCode.PARAM_INVALID, msg);
    }

    /**
     * 处理 @Validated 校验失败（Query参数校验）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleConstraintViolation(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败 - {}", msg);
        return Result.fail(ErrorCode.PARAM_INVALID, msg);
    }

    /**
     * 处理表单绑定校验失败
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBindException(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("表单绑定失败 - {}", msg);
        return Result.fail(ErrorCode.PARAM_INVALID, msg);
    }

    /**
     * 缺少必要请求参数
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMissingParam(MissingServletRequestParameterException e) {
        return Result.fail(ErrorCode.PARAM_MISSING, "缺少参数: " + e.getParameterName());
    }

    /**
     * 请求体格式错误（JSON解析失败）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败 - {}", e.getMessage());
        return Result.fail(ErrorCode.PARAM_INVALID, "请求数据格式不正确");
    }

    // ==================== 404 ====================

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<?> handleNoHandlerFound(NoHandlerFoundException e) {
        return Result.fail(ErrorCode.NOT_FOUND, "接口不存在: " + e.getRequestURL());
    }

    // ==================== 请求方法不匹配 ====================

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return Result.fail(ErrorCode.PARAM_INVALID, "不支持的请求方法: " + e.getMethod());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public Result<?> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e) {
        return Result.fail(ErrorCode.PARAM_INVALID, "不支持的 Content-Type");
    }

    // ==================== 文件上传 ====================

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<?> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        return Result.fail(ErrorCode.FILE_TOO_LARGE, "文件大小超出限制（最大 " +
                e.getMaxUploadSize() / 1024 / 1024 + "MB）");
    }

    // ==================== 兜底异常 ====================

    /**
     * 处理所有未被上面捕获的异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e, HttpServletRequest request) {
        log.error("未处理异常 - {} {} -> {}", request.getMethod(), request.getRequestURI(), e.getMessage(), e);
        return Result.fail(ErrorCode.INTERNAL_ERROR);
    }
}
