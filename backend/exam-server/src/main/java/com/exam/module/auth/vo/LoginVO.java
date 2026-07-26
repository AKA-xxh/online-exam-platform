package com.exam.module.auth.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 登录成功返回 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    /** Access Token（放在请求头中携带） */
    private String accessToken;

    /** Token 类型 */
    private String tokenType;

    /** Token 有效期（秒） */
    private Long expiresIn;

    /** 用户基本信息 */
    private UserInfoVO userInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfoVO {
        private Long userId;
        private String username;
        private String realName;
        private String nickname;
        private String avatar;
        private String phone;
        private String email;
        private Integer userType;
        private List<String> roles;
        private List<String> permissions;
    }
}
