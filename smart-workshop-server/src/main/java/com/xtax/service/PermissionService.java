package com.xtax.service;

import com.xtax.entity.Permission;
import com.xtax.vo.PermissionVO;

import java.util.List;
import java.util.Map;

/**
 * 权限服务接口
 * 提供权限管理相关的业务逻辑
 */
public interface PermissionService {
    /**
     * 查询所有权限，可按模块筛选
     */
    List<Permission> getAllPermission(String module);

    /**
     * 根据ID查询权限详情
     */
    Permission getPermissionById(Integer id);

    /**
     * 按模块分组查询权限
     */
    Map<String, List<PermissionVO>> getPermissionsGrouped();
}