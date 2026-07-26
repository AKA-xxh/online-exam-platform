package com.exam.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户实体
 */
@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名（登录用） */
    private String username;

    /** 密码（BCrypt 加密） */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 昵称 */
    private String nickname;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 头像 URL */
    private String avatar;

    /** 性别: 0未知 1男 2女 */
    private Integer gender;

    /** 用户类型: 1学员 2教师 3管理员 */
    private Integer userType;

    /** 状态: 0禁用 1正常 */
    private Integer status;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;

    /** 最后登录 IP */
    private String lastLoginIp;

    /** 逻辑删除: 0否 1是 */
    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
