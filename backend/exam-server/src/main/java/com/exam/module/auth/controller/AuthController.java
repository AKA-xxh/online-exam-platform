package com.exam.module.auth.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.exam.common.result.Result;
import com.exam.module.auth.dto.LoginDTO;
import com.exam.module.auth.dto.RegisterDTO;
import com.exam.module.auth.service.AuthService;
import com.exam.module.auth.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * <p>
 * 提供登录、注册、Token刷新、登出、修改密码等认证相关接口。
 * 这些接口在白名单中，无需登录即可访问（除修改密码外）。
 */
@Tag(name = "认证模块", description = "登录、注册、Token 管理")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "账号密码登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = authService.login(loginDTO);
        return Result.ok("登录成功", loginVO);
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO registerDTO) {
        authService.register(registerDTO);
        return Result.ok("注册成功", null);
    }

    @Operation(summary = "刷新 Token")
    @PostMapping("/refresh-token")
    public Result<LoginVO> refreshToken() {
        LoginVO loginVO = authService.refreshToken(null);
        return Result.ok(loginVO);
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout(StpUtil.getLoginIdAsLong());
        return Result.ok();
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        authService.changePassword(StpUtil.getLoginIdAsLong(), oldPassword, newPassword);
        return Result.ok("密码修改成功，请重新登录", null);
    }

    @Operation(summary = "获取图形验证码")
    @GetMapping("/captcha")
    public Result<String> captcha() {
        String captchaKey = authService.generateCaptcha();
        return Result.ok(captchaKey);
    }

    @Operation(summary = "检查登录状态")
    @GetMapping("/check")
    public Result<Boolean> checkLogin() {
        return Result.ok(StpUtil.isLogin());
    }
}
