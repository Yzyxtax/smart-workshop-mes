package com.xtax.service;

import com.xtax.dto.AssignRoleDTO;
import com.xtax.vo.RoleVO;
import com.xtax.vo.UserVO;

import java.util.List;

/**
 * 用户角色服务接口
 * 提供用户角色分配相关的业务逻辑
 */
public interface UserRoleService {
    /**
     * 查询用户的角色列表
     */
    List<RoleVO> getRolesByUserId(Integer userId);

    /**
     * 查询角色下的用户列表
     */
    List<UserVO> getUsersByRoleId(Integer roleId);

    /**
     * 为用户分配角色
     */
    int assignRoles(AssignRoleDTO dto);

    /**
     * 移除用户角色
     */
    int removeRoles(Integer userId, List<Integer> roleIds);
}