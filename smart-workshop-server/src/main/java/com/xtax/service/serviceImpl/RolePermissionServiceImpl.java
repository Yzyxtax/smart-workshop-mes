package com.xtax.service.serviceImpl;

import com.xtax.dto.AssignPermissionDTO;
import com.xtax.entity.RolePermission;
import com.xtax.mapper.RolePermissionMapper;
import com.xtax.service.RolePermissionService;
import com.xtax.vo.PermissionVO;
import com.xtax.vo.RoleVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色权限服务实现类
 * 实现角色权限分配相关的业务逻辑
 */
@Slf4j
@Service
public class RolePermissionServiceImpl implements RolePermissionService {

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    /**
     * 查询角色的权限列表
     */
    @Override
    public List<PermissionVO> getPermissionsByRoleId(Integer roleId) {
        log.info("查询角色权限，roleId：{}", roleId);
        return rolePermissionMapper.getPermissionsByRoleId(roleId);
    }

    /**
     * 查询权限所属的角色列表
     */
    @Override
    public List<RoleVO> getRolesByPermissionId(Integer permissionId) {
        log.info("查询权限角色，permissionId：{}", permissionId);
        return rolePermissionMapper.getRolesByPermissionId(permissionId);
    }

    /**
     * 为角色分配权限
     */
    @Override
    @Transactional
    public int assignPermissions(AssignPermissionDTO dto) {
        log.info("为角色分配权限：{}", dto);
        // 防御性检查：permissionIds为null时直接返回
        if (dto.getPermissionIds() == null) {
            return 0;
        }
        // 过滤已存在的权限关联，避免重复插入
        List<Integer> newPermissionIds = new ArrayList<>();
        for (Integer permissionId : dto.getPermissionIds()) {
            RolePermission existing = rolePermissionMapper.getRolePermission(dto.getRoleId(), permissionId);
            if (existing == null) {
                newPermissionIds.add(permissionId);
            }
        }
        
        if (newPermissionIds.isEmpty()) {
            return 0;
        }
        
        AssignPermissionDTO newDto = new AssignPermissionDTO();
        newDto.setRoleId(dto.getRoleId());
        newDto.setPermissionIds(newPermissionIds);
        return rolePermissionMapper.assignPermissions(newDto);
    }

    /**
     * 移除角色权限
     */
    @Override
    public int removePermissions(Integer roleId, List<Integer> permissionIds) {
        log.info("移除角色权限，roleId：{}，permissionIds：{}", roleId, permissionIds);
        return rolePermissionMapper.removePermissions(roleId, permissionIds);
    }
}