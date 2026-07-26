package com.exam.module.auth.service;

import com.exam.module.auth.dto.LoginDTO;
import com.exam.module.auth.dto.RegisterDTO;
import com.exam.module.auth.vo.LoginVO;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 账号密码登录
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 用户注册
     */
    void register(RegisterDTO registerDTO);

    /**
     * 刷新 Token
     */
    LoginVO refreshToken(String refreshToken);

    /**
     * 退出登录（Token 加入黑名单）
     */
    void logout(Long userId);

    /**
     * 修改密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 生成图形验证码
     */
    String generateCaptcha();
}
