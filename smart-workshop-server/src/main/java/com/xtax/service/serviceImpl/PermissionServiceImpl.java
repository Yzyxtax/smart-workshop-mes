package com.xtax.service.serviceImpl;

import com.xtax.entity.Permission;
import com.xtax.mapper.PermissionMapper;
import com.xtax.service.PermissionService;
import com.xtax.vo.PermissionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 权限服务实现类
 * 实现权限管理相关的业务逻辑
 */
@Slf4j
@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private PermissionMapper permissionMapper;

    /**
     * 查询所有权限，可按模块筛选
     */
    @Override
    public List<Permission> getAllPermission(String module) {
        log.info("查询权限列表，模块：{}", module);
        return permissionMapper.getAllPermission(module);
    }

    /**
     * 根据ID查询权限详情
     */
    @Override
    public Permission getPermissionById(Integer id) {
        log.info("查询权限详情，id：{}", id);
        return permissionMapper.getPermissionById(id);
    }

    /**
     * 按模块分组查询权限
     */
    @Override
    public Map<String, List<PermissionVO>> getPermissionsGrouped() {
        log.info("按模块分组查询权限");
        List<Permission> permissions = permissionMapper.getAllPermission(null);
        Map<String, List<PermissionVO>> groupedMap = new HashMap<>();
        
        for (Permission permission : permissions) {
            String module = permission.getModule();
            if (!groupedMap.containsKey(module)) {
                groupedMap.put(module, new ArrayList<>());
            }
            PermissionVO vo = new PermissionVO();
            vo.setId(permission.getId());
            vo.setPermissionCode(permission.getPermissionCode());
            vo.setPermissionName(permission.getPermissionName());
            vo.setModule(permission.getModule());
            vo.setDescription(permission.getDescription());
            groupedMap.get(module).add(vo);
        }
        
        return groupedMap;
    }
}