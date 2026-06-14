package com.xtax.mapper;

import com.xtax.dto.AssignRoleDTO;
import com.xtax.entity.UserRole;
import com.xtax.vo.RoleVO;
import com.xtax.vo.UserVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用户角色Mapper接口
 * 用于用户角色关联的数据库操作
 */
@Mapper
public interface UserRoleMapper {
    /**
     * 查询用户的角色列表
     */
    List<RoleVO> getRolesByUserId(Integer userId);

    /**
     * 查询角色下的用户列表
     */
    List<UserVO> getUsersByRoleId(Integer roleId);

    /**
     * 为用户分配角色（批量插入）
     */
    int assignRoles(AssignRoleDTO dto);

    /**
     * 移除用户角色
     */
    int removeRoles(Integer userId, List<Integer> roleIds);

    /**
     * 查询用户角色关联是否存在
     */
    UserRole getUserRole(Integer userId, Integer roleId);

    /**
     * 删除用户的所有角色关联（用于删除用户时级联删除）
     */
    int deleteUserRolesByUserId(Integer userId);

    /**
     * 删除角色的所有用户关联（用于删除角色时级联删除）
     */
    int deleteUserRolesByRoleId(Integer roleId);
}