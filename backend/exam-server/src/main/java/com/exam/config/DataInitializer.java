package com.exam.config;

import cn.dev33.satoken.secure.BCrypt;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.exam.module.user.entity.SysUser;
import com.exam.module.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器 — 应用启动时确保默认账号的密码正确
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SysUserMapper userMapper;

    @Override
    public void run(String... args) {
        ensureUser("admin", "admin123", "系统管理员", "Admin", 3);
        ensureUser("teacher", "teacher123", "张老师", "Teacher Zhang", 2);
        ensureUser("student", "student123", "李同学", "Student Li", 1);
    }

    private void ensureUser(String username, String rawPwd, String realName, String nickname, int userType) {
        SysUser user = userMapper.selectOne(
                Wrappers.lambdaQuery(SysUser.class).eq(SysUser::getUsername, username));
        String hashedPwd = BCrypt.hashpw(rawPwd);
        if (user == null) {
            user = new SysUser();
            user.setUsername(username);
            user.setPassword(hashedPwd);
            user.setRealName(realName);
            user.setNickname(nickname);
            user.setUserType(userType);
            user.setStatus(1);
            userMapper.insert(user);
            log.info("默认账号已创建: {} / {}", username, rawPwd);
        } else {
            // 每次启动都重置为正确密码，确保与 bcrypt 哈希一致
            user.setPassword(hashedPwd);
            userMapper.updateById(user);
            log.info("默认账号密码已刷新: {}", username);
        }
    }
}
