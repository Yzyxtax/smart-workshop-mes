package com.xtax.service;

import com.xtax.vo.UserPermissionVO;

import java.util.Set;

/**
 * 权限校验服务接口
 * 提供权限校验相关的业务逻辑
 */
public interface AuthService {
    /**
     * 获取当前用户的权限信息
     */
    UserPermissionVO getCurrentUserPermissions(Integer userId);

    /**
     * 校验用户是否拥有指定权限
     */
    boolean checkPermission(Integer userId, String permissionCode);

    /**
     * 获取用户的所有权限编码
     */
    Set<String> getUserPermissionCodes(Integer userId);
}