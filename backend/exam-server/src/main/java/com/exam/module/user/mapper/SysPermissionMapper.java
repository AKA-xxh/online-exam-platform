package com.exam.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.module.user.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统权限 Mapper
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    /**
     * 查询用户拥有的权限编码列表
     */
    @Select("SELECT DISTINCT p.perm_code FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<String> selectPermCodesByUserId(@Param("userId") Long userId);
}
