package com.xtax.service;

import com.xtax.dto.RoleQueryParam;
import com.xtax.entity.Role;
import com.xtax.vo.RoleVO;
import com.xtax.vo.ResultPage;

import java.util.List;

/**
 * 角色服务接口
 * 提供角色管理相关的业务逻辑
 */
public interface RoleService {
    /**
     * 条件分页查询角色列表
     */
    ResultPage<Role> getAllRole(RoleQueryParam param);

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
}