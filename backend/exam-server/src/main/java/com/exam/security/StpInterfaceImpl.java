package com.exam.security;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.exam.module.user.mapper.SysRoleMapper;
import com.exam.module.user.mapper.SysPermissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 权限加载实现
 * <p>
 * 每次权限校验时，Sa-Token 调用此实现从数据库加载当前用户的角色和权限列表。
 * 类比：就像门禁系统，刷卡时去查数据库看这个人有哪些权限。
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<String> perms = permissionMapper.selectPermCodesByUserId(userId);
        return perms != null ? perms : new ArrayList<>();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<String> roles = roleMapper.selectRoleCodesByUserId(userId);
        return roles != null ? roles : new ArrayList<>();
    }
}
