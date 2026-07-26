package com.exam.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统权限实体
 */
@Data
@TableName("sys_permission")
public class SysPermission {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 父权限 ID */
    private Long parentId;

    /** 权限名称 */
    private String permName;

    /** 权限编码 */
    private String permCode;

    /** 类型: 1菜单 2按钮 3接口 */
    private Integer permType;

    /** 路由路径 */
    private String path;

    /** 前端组件路径 */
    private String component;

    /** 菜单图标 */
    private String icon;

    /** 排序 */
    private Integer sortOrder;

    /** 是否可见 */
    private Integer isVisible;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
