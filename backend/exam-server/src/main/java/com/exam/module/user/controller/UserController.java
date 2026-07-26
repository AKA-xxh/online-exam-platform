package com.exam.module.user.controller;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import com.exam.common.result.Result;
import com.exam.common.exception.BusinessException;
import com.exam.common.result.ErrorCode;
import com.exam.module.user.entity.SysUser;
import com.exam.module.user.mapper.SysUserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "个人中心", description = "当前用户信息查看、编辑、修改密码")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final SysUserMapper userMapper;

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<SysUser> me() {
        SysUser user = userMapper.selectById(StpUtil.getLoginIdAsLong());
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        user.setPassword(null); // 不返回密码
        return Result.ok(user);
    }

    @Operation(summary = "更新当前用户信息")
    @PutMapping("/me")
    public Result<Void> updateMe(@RequestBody Map<String,Object> data) {
        SysUser user = userMapper.selectById(StpUtil.getLoginIdAsLong());
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        if (data.containsKey("realName")) user.setRealName(data.get("realName").toString());
        if (data.containsKey("phone")) user.setPhone(data.get("phone").toString());
        if (data.containsKey("email")) user.setEmail(data.get("email").toString());
        userMapper.updateById(user);
        return Result.ok("保存成功", null);
    }

    @Operation(summary = "修改密码")
    @PutMapping("/me/password")
    public Result<Void> changePassword(@RequestBody Map<String,String> data) {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        if (!BCrypt.checkpw(data.get("oldPassword"), user.getPassword()))
            throw new BusinessException(ErrorCode.LOGIN_FAILED, "原密码错误");
        user.setPassword(BCrypt.hashpw(data.get("newPassword")));
        userMapper.updateById(user);
        StpUtil.logout(userId);
        return Result.ok("密码已修改，请重新登录", null);
    }
}
