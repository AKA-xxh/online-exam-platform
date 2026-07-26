package com.exam.security;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.exam.common.result.ErrorCode;
import com.exam.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Sa-Token 异常处理
 * <p>
 * 将 Sa-Token 的认证/权限异常转换为统一的 Result 格式返回。
 * Order 设为高优先级，确保在 GlobalExceptionHandler 之前处理。
 */
@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SaTokenExceptionHandler {

    @ExceptionHandler(NotLoginException.class)
    public Result<?> handleNotLogin(NotLoginException e) {
        log.warn("未登录访问: {}", e.getMessage());
        return Result.fail(ErrorCode.UNAUTHORIZED);
    }

    @ExceptionHandler(NotPermissionException.class)
    public Result<?> handleNotPermission(NotPermissionException e) {
        log.warn("权限不足: {}", e.getMessage());
        return Result.fail(ErrorCode.NO_PERMISSION);
    }

    @ExceptionHandler(NotRoleException.class)
    public Result<?> handleNotRole(NotRoleException e) {
        log.warn("角色不匹配: {}", e.getMessage());
        return Result.fail(ErrorCode.FORBIDDEN);
    }
}
