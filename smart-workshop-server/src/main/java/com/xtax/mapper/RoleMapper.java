package com.xtax.mapper;

import com.xtax.dto.RoleQueryParam;
import com.xtax.entity.Role;
import com.xtax.vo.RoleVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 角色Mapper接口
 * 用于角色相关的数据库操作
 */
@Mapper
public interface RoleMapper {
    /**
     * 条件分页查询角色列表
     */
    List<Role> getAllRole(RoleQueryParam param);

    /**
     * 根据ID查询角色详情
     */
    RoleVO getRoleById(Integer id);

    /**
     * 添加角色
     */
    int addRole(Role role);

    /**
     * 更新角色
     */
    int updateRole(Role role);

    /**
     * 批量删除角色
     */
    int deleteRoles(List<Integer> ids);

    /**
     * 根据角色编码查询角色
     */
    Role getRoleByCode(String roleCode);

    /**
     * 统计角色总数
     */
    Long countRole(RoleQueryParam param);
}