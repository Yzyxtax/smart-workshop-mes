package com.xtax.mapper;

import com.xtax.entity.Permission;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 权限Mapper接口
 * 用于权限相关的数据库操作
 */
@Mapper
public interface PermissionMapper {
    /**
     * 查询所有权限
     */
    List<Permission> getAllPermission(String module);

    /**
     * 根据ID查询权限
     */
    Permission getPermissionById(Integer id);

    /**
     * 根据权限编码查询权限
     */
    Permission getPermissionByCode(String permissionCode);

    /**
     * 查询用户的所有权限编码
     */
    List<String> getUserPermissionCodes(Integer userId);
}