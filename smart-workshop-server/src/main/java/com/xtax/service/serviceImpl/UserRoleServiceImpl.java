package com.xtax.service.serviceImpl;

import com.xtax.dto.AssignRoleDTO;
import com.xtax.entity.UserRole;
import com.xtax.mapper.UserRoleMapper;
import com.xtax.service.UserRoleService;
import com.xtax.vo.RoleVO;
import com.xtax.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户角色服务实现类
 * 实现用户角色分配相关的业务逻辑
 */
@Slf4j
@Service
public class UserRoleServiceImpl implements UserRoleService {

    @Autowired
    private UserRoleMapper userRoleMapper;

    /**
     * 查询用户的角色列表
     */
    @Override
    public List<RoleVO> getRolesByUserId(Integer userId) {
        log.info("查询用户角色，userId：{}", userId);
        return userRoleMapper.getRolesByUserId(userId);
    }

    /**
     * 查询角色下的用户列表
     */
    @Override
    public List<UserVO> getUsersByRoleId(Integer roleId) {
        log.info("查询角色用户，roleId：{}", roleId);
        return userRoleMapper.getUsersByRoleId(roleId);
    }

    /**
     * 为用户分配角色
     */
    @Override
    @Transactional
    public int assignRoles(AssignRoleDTO dto) {
        log.info("为用户分配角色：{}", dto);
        // 防御性检查：roleIds为null时直接返回
        if (dto.getRoleIds() == null) {
            return 0;
        }
        // 过滤已存在的角色关联，避免重复插入
        List<Integer> newRoleIds = new ArrayList<>();
        for (Integer roleId : dto.getRoleIds()) {
            UserRole existing = userRoleMapper.getUserRole(dto.getUserId(), roleId);
            if (existing == null) {
                newRoleIds.add(roleId);
            }
        }
        
        if (newRoleIds.isEmpty()) {
            return 0;
        }
        
        AssignRoleDTO newDto = new AssignRoleDTO();
        newDto.setUserId(dto.getUserId());
        newDto.setRoleIds(newRoleIds);
        return userRoleMapper.assignRoles(newDto);
    }

    /**
     * 移除用户角色
     */
    @Override
    public int removeRoles(Integer userId, List<Integer> roleIds) {
        log.info("移除用户角色，userId：{}，roleIds：{}", userId, roleIds);
        return userRoleMapper.removeRoles(userId, roleIds);
    }
}