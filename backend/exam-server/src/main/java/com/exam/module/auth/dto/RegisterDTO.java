package com.exam.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求 DTO
 */
@Data
public class RegisterDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 32, message = "用户名长度需在3-32位之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度需在6-32位之间")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    /** 手机号（可选） */
    private String phone;

    /** 邮箱（可选） */
    private String email;

    /** 短信/邮箱验证码（可选，如果配置了验证码校验） */
    private String verifyCode;
}
