package com.xtax.service.serviceImpl;

import com.xtax.exception.BusinessException;
import com.xtax.mapper.PermissionMapper;
import com.xtax.mapper.userMapper;
import com.xtax.service.AuthService;
import com.xtax.service.UserRoleService;
import com.xtax.vo.RoleVO;
import com.xtax.vo.UserPermissionVO;
import com.xtax.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 权限校验服务实现类
 * 实现权限校验相关的业务逻辑
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private userMapper userMapper;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private PermissionMapper permissionMapper;

    /**
     * 获取当前用户的权限信息
     */
    @Override
    public UserPermissionVO getCurrentUserPermissions(Integer userId) {
        log.info("获取用户权限信息，userId：{}", userId);
        
        // 查询用户基本信息
        UserVO user = userMapper.getUserById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 查询用户角色
        List<RoleVO> roles = userRoleService.getRolesByUserId(userId);
        
        // 查询用户权限编码
        Set<String> permissions = getUserPermissionCodes(userId);
        
        UserPermissionVO vo = new UserPermissionVO();
        vo.setUserId(userId);
        vo.setUsername(user.getUserName());
        vo.setName(user.getName());
        vo.setRoles(roles);
        vo.setPermissions(new ArrayList<>(permissions));
        
        return vo;
    }

    /**
     * 校验用户是否拥有指定权限
     */
    @Override
    public boolean checkPermission(Integer userId, String permissionCode) {
        log.info("校验用户权限，userId：{}，permissionCode：{}", userId, permissionCode);
        Set<String> permissions = getUserPermissionCodes(userId);
        return permissions.contains(permissionCode);
    }

    /**
     * 获取用户的所有权限编码
     */
    @Override
    public Set<String> getUserPermissionCodes(Integer userId) {
        log.info("获取用户权限编码，userId：{}", userId);
        List<String> permissionCodes = permissionMapper.getUserPermissionCodes(userId);
        return new HashSet<>(permissionCodes);
    }
}