package com.exam.module.user.controller;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.constant.Constants;
import com.exam.common.result.PageResult;
import com.exam.common.result.Result;
import com.exam.common.exception.BusinessException;
import com.exam.common.result.ErrorCode;
import com.exam.module.user.entity.SysUser;
import com.exam.module.user.entity.SysRole;
import com.exam.module.user.mapper.SysUserMapper;
import com.exam.module.user.mapper.SysRoleMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "用户管理", description = "用户列表、新增、编辑、删除、状态切换、密码重置")
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;

    @Operation(summary = "用户列表（分页 + 搜索 + 角色筛选）")
    @GetMapping
    public Result<PageResult<Map<String,Object>>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer userType,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        LambdaQueryWrapper<SysUser> qw = Wrappers.lambdaQuery(SysUser.class)
                .eq(userType != null, SysUser::getUserType, userType)
                .eq(status != null, SysUser::getStatus, status)
                .and(keyword != null && !keyword.isEmpty(), w -> w
                        .like(SysUser::getUsername, keyword)
                        .or().like(SysUser::getRealName, keyword)
                        .or().like(SysUser::getPhone, keyword)
                        .or().like(SysUser::getEmail, keyword))
                .orderByDesc(SysUser::getCreateTime);

        Page<SysUser> mp = userMapper.selectPage(new Page<>(page, pageSize), qw);

        List<Map<String,Object>> records = mp.getRecords().stream().map(u -> {
            Map<String,Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("realName", u.getRealName());
            m.put("nickname", u.getNickname());
            m.put("phone", u.getPhone());
            m.put("email", u.getEmail());
            m.put("avatar", u.getAvatar());
            m.put("gender", u.getGender());
            m.put("userType", u.getUserType());
            m.put("userTypeName", u.getUserType() == 1 ? "学员" : u.getUserType() == 2 ? "教师" : "管理员");
            m.put("status", u.getStatus());
            m.put("lastLoginTime", u.getLastLoginTime());
            m.put("lastLoginIp", u.getLastLoginIp());
            m.put("createTime", u.getCreateTime());
            return m;
        }).collect(Collectors.toList());

        return Result.ok(new PageResult<>(records, mp.getTotal(), page, pageSize, mp.getPages()));
    }

    @Operation(summary = "用户详情")
    @GetMapping("/{id}")
    public Result<Map<String,Object>> detail(@PathVariable Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);

        Map<String,Object> m = new HashMap<>();
        m.put("id", user.getId());
        m.put("username", user.getUsername());
        m.put("realName", user.getRealName());
        m.put("nickname", user.getNickname());
        m.put("phone", user.getPhone());
        m.put("email", user.getEmail());
        m.put("userType", user.getUserType());
        m.put("status", user.getStatus());
        m.put("lastLoginTime", user.getLastLoginTime());

        // 查询用户角色
        List<String> roles = roleMapper.selectRoleCodesByUserId(id);
        m.put("roles", roles);

        return Result.ok(m);
    }

    @Operation(summary = "获取角色列表（供下拉选择）")
    @GetMapping("/roles")
    public Result<List<SysRole>> roles() {
        return Result.ok(roleMapper.selectList(Wrappers.lambdaQuery(SysRole.class).eq(SysRole::getStatus, 1)));
    }

    @Operation(summary = "新增用户")
    @PostMapping
    @Transactional
    public Result<Map<String,Object>> create(@RequestBody Map<String,Object> data) {
        String username = data.get("username").toString();
        Long count = userMapper.selectCount(Wrappers.lambdaQuery(SysUser.class).eq(SysUser::getUsername, username));
        if (count > 0) throw new BusinessException(ErrorCode.PHONE_EXISTS, "用户名已存在");

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(BCrypt.hashpw(data.getOrDefault("password", Constants.DEFAULT_PASSWORD).toString()));
        user.setRealName(data.getOrDefault("realName", "").toString());
        user.setNickname(data.getOrDefault("nickname", data.getOrDefault("realName", "")).toString());
        user.setPhone(data.getOrDefault("phone", "").toString());
        user.setEmail(data.getOrDefault("email", "").toString());
        user.setUserType(Integer.valueOf(data.getOrDefault("userType", 1).toString()));
        user.setStatus(Integer.valueOf(data.getOrDefault("status", 1).toString()));
        userMapper.insert(user);

        Map<String,Object> result = new HashMap<>();
        result.put("id", user.getId());
        return Result.ok("创建成功", result);
    }

    @Operation(summary = "编辑用户")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Map<String,Object> data) {
        SysUser user = userMapper.selectById(id);
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);

        if (data.containsKey("realName")) user.setRealName(data.get("realName").toString());
        if (data.containsKey("nickname")) user.setNickname(data.get("nickname").toString());
        if (data.containsKey("phone")) user.setPhone(data.get("phone").toString());
        if (data.containsKey("email")) user.setEmail(data.get("email").toString());
        if (data.containsKey("userType")) user.setUserType(Integer.valueOf(data.get("userType").toString()));
        userMapper.updateById(user);
        return Result.ok("保存成功", null);
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (id == 1L) throw new BusinessException(ErrorCode.PARAM_INVALID, "不能删除超级管理员");
        userMapper.deleteById(id);
        return Result.ok("删除成功", null);
    }

    @Operation(summary = "启用/禁用用户")
    @PatchMapping("/{id}/status")
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        if (id == 1L) throw new BusinessException(ErrorCode.PARAM_INVALID, "不能禁用超级管理员");
        SysUser user = userMapper.selectById(id);
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        user.setStatus(status);
        userMapper.updateById(user);
        return Result.ok(status == 1 ? "已启用" : "已禁用", null);
    }

    @Operation(summary = "重置密码")
    @PatchMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        user.setPassword(BCrypt.hashpw(Constants.DEFAULT_PASSWORD));
        userMapper.updateById(user);
        // 强制该用户重新登录
        StpUtil.logout(id);
        return Result.ok("密码已重置为 " + Constants.DEFAULT_PASSWORD, null);
    }
}
