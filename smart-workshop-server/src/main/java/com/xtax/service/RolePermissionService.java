package com.xtax.service;

import com.xtax.dto.AssignPermissionDTO;
import com.xtax.vo.PermissionVO;
import com.xtax.vo.RoleVO;

import java.util.List;

/**
 * 角色权限服务接口
 * 提供角色权限分配相关的业务逻辑
 */
public interface RolePermissionService {
    /**
     * 查询角色的权限列表
     */
    List<PermissionVO> getPermissionsByRoleId(Integer roleId);

    /**
     * 查询权限所属的角色列表
     */
    List<RoleVO> getRolesByPermissionId(Integer permissionId);

    /**
     * 为角色分配权限
     */
    int assignPermissions(AssignPermissionDTO dto);

    /**
     * 移除角色权限
     */
    int removePermissions(Integer roleId, List<Integer> permissionIds);
}