package com.xtax.service.serviceImpl;

import com.xtax.dto.RoleQueryParam;
import com.xtax.entity.Role;
import com.xtax.exception.BusinessException;
import com.xtax.mapper.RoleMapper;
import com.xtax.mapper.RolePermissionMapper;
import com.xtax.mapper.UserRoleMapper;
import com.xtax.service.RoleService;
import com.xtax.vo.RoleVO;
import com.xtax.vo.ResultPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色服务实现类
 * 实现角色管理相关的业务逻辑
 */
@Slf4j
@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    /**
     * 条件分页查询角色列表
     */
    @Override
    public ResultPage<Role> getAllRole(RoleQueryParam param) {
        log.info("查询角色列表，参数：{}", param);
        List<Role> roles = roleMapper.getAllRole(param);
        // 查询符合条件的总记录数
        ResultPage<Role> resultPage = new ResultPage<>();
        resultPage.setRows(roles);
        resultPage.setTotal(roleMapper.countRole(param));
        return resultPage;
    }

    /**
     * 根据ID查询角色详情
     */
    @Override
    public RoleVO getRoleById(Integer id) {
        log.info("查询角色详情，id：{}", id);
        return roleMapper.getRoleById(id);
    }

    /**
     * 添加角色
     */
    @Override
    public int addRole(Role role) {
        log.info("添加角色：{}", role);
        // 检查角色编码是否已存在
        Role existingRole = roleMapper.getRoleByCode(role.getRoleCode());
        if (existingRole != null) {
            throw new BusinessException("角色编码已存在");
        }
        return roleMapper.addRole(role);
    }

    /**
     * 更新角色
     * 校验角色编码唯一性，避免与已有角色冲突
     */
    @Override
    public int updateRole(Role role) {
        log.info("更新角色：{}", role);
        // 如果传入了角色编码，校验是否与已有角色冲突
        if (role.getRoleCode() != null && !role.getRoleCode().isEmpty()) {
            Role existingRole = roleMapper.getRoleByCode(role.getRoleCode());
            if (existingRole != null && !existingRole.getId().equals(role.getId())) {
                throw new BusinessException("角色编码已存在");
            }
        }
        return roleMapper.updateRole(role);
    }

    /**
     * 批量删除角色
     * 级联删除角色权限关联和用户角色关联，保证数据一致性
     */
    @Override
    @Transactional
    public int deleteRoles(List<Integer> ids) {
        log.info("删除角色，ids：{}", ids);
        // 先级联删除角色权限关联和用户角色关联
        for (Integer id : ids) {
            rolePermissionMapper.deleteRolePermissionsByRoleId(id);
            userRoleMapper.deleteUserRolesByRoleId(id);
        }
        // 再删除角色
        return roleMapper.deleteRoles(ids);
    }
}