package com.exam.module.auth.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.exam.common.constant.Constants;
import com.exam.common.enums.UserTypeEnum;
import com.exam.common.exception.BusinessException;
import com.exam.common.result.ErrorCode;
import com.exam.module.auth.dto.LoginDTO;
import com.exam.module.auth.dto.RegisterDTO;
import com.exam.module.auth.service.AuthService;
import com.exam.module.auth.vo.LoginVO;
import com.exam.module.user.entity.SysRole;
import com.exam.module.user.entity.SysUser;
import com.exam.module.user.mapper.SysRoleMapper;
import com.exam.module.user.mapper.SysPermissionMapper;
import com.exam.module.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 1. 查询用户
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, loginDTO.getUsername()));

        if (user == null) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED, "用户名或密码错误");
        }

        // 2. 检查账号状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }

        // 3. 校验密码
        if (!BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED, "用户名或密码错误");
        }

        // 4. 登录（Sa-Token）
        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();

        // 5. 加载角色和权限
        List<String> roles = roleMapper.selectRoleCodesByUserId(user.getId());
        List<String> permissions = permissionMapper.selectPermCodesByUserId(user.getId());

        log.info("用户 {} ({}) 登录成功", user.getUsername(), user.getRealName());

        // 6. 构建返回
        return LoginVO.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(StpUtil.getTokenTimeout())
                .userInfo(LoginVO.UserInfoVO.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .realName(user.getRealName())
                        .nickname(user.getNickname())
                        .avatar(user.getAvatar())
                        .phone(user.getPhone())
                        .email(user.getEmail())
                        .userType(user.getUserType())
                        .roles(roles)
                        .permissions(permissions)
                        .build())
                .build();
    }

    @Override
    @Transactional
    public void register(RegisterDTO registerDTO) {
        // 1. 检查用户名是否已存在
        Long count = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, registerDTO.getUsername()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.PHONE_EXISTS, "用户名已被注册");
        }

        // 2. 创建用户
        SysUser user = new SysUser();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(BCrypt.hashpw(registerDTO.getPassword()));
        user.setRealName(registerDTO.getRealName());
        user.setNickname(registerDTO.getRealName());
        user.setPhone(registerDTO.getPhone());
        user.setEmail(registerDTO.getEmail());
        user.setUserType(UserTypeEnum.STUDENT.getCode());
        user.setStatus(1);
        userMapper.insert(user);

        // 3. 分配默认角色（学员）
        // 实际项目中应通过用户角色关联表插入
        // sysUserRoleMapper.insert(new SysUserRole(user.getId(), defaultRoleId));

        log.info("新用户注册: {} ({})", user.getUsername(), user.getRealName());
    }

    @Override
    public LoginVO refreshToken(String refreshToken) {
        // Sa-Token 自动处理 Token 续期
        // 如果 Token 快过期，前端调用此接口续期
        Long userId = StpUtil.getLoginIdAsLong();
        StpUtil.renewTimeout(7200); // 续期 2 小时

        String token = StpUtil.getTokenValue();
        SysUser user = userMapper.selectById(userId);

        return LoginVO.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(StpUtil.getTokenTimeout())
                .build();
    }

    @Override
    public void logout(Long userId) {
        StpUtil.logout(userId);
        log.info("用户 {} 退出登录", userId);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 校验旧密码
        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED, "原密码错误");
        }

        // 更新密码
        user.setPassword(BCrypt.hashpw(newPassword));
        userMapper.updateById(user);

        // 修改密码后强制重新登录
        StpUtil.logout(userId);
        log.info("用户 {} 修改密码成功，已强制登出", userId);
    }

    @Override
    public String generateCaptcha() {
        // 生成简单的验证码 key（实际项目应集成图形验证码库如 EasyCaptcha）
        return IdUtil.fastSimpleUUID();
    }
}
