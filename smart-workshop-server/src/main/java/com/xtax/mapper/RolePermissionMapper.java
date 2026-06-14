package com.xtax.mapper;

import com.xtax.dto.AssignPermissionDTO;
import com.xtax.entity.RolePermission;
import com.xtax.vo.PermissionVO;
import com.xtax.vo.RoleVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 角色权限Mapper接口
 * 用于角色权限关联的数据库操作
 */
@Mapper
public interface RolePermissionMapper {
    /**
     * 查询角色的权限列表
     */
    List<PermissionVO> getPermissionsByRoleId(Integer roleId);

    /**
     * 查询权限所属的角色列表
     */
    List<RoleVO> getRolesByPermissionId(Integer permissionId);

    /**
     * 为角色分配权限（批量插入）
     */
    int assignPermissions(AssignPermissionDTO dto);

    /**
     * 移除角色权限
     */
    int removePermissions(Integer roleId, List<Integer> permissionIds);

    /**
     * 查询角色权限关联是否存在
     */
    RolePermission getRolePermission(Integer roleId, Integer permissionId);

    /**
     * 删除角色的所有权限关联（用于删除角色时级联删除）
     */
    int deleteRolePermissionsByRoleId(Integer roleId);
}