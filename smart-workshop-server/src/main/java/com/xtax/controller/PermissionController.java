package com.xtax.controller;

import com.xtax.entity.Permission;
import com.xtax.service.PermissionService;
import com.xtax.vo.PermissionVO;
import com.xtax.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 权限管理控制器
 * 提供权限相关的REST接口
 */
@Slf4j
@RestController
@RequestMapping("/permission")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    /**
     * 查询权限列表
     */
    @GetMapping
    public Result getAllPermission(@RequestParam(required = false) String module) {
        log.info("查询权限列表，模块：{}", module);
        List<Permission> permissions = permissionService.getAllPermission(module);
        return Result.success(permissions);
    }

    /**
     * 查询权限详情
     */
    @GetMapping("/{id}")
    public Result getPermissionById(@PathVariable Integer id) {
        log.info("查询权限详情，id：{}", id);
        Permission permission = permissionService.getPermissionById(id);
        if (permission != null) {
            return Result.success(permission);
        }
        return Result.error("查询失败");
    }

    /**
     * 按模块分组查询权限
     */
    @GetMapping("/grouped")
    public Result getPermissionsGrouped() {
        log.info("按模块分组查询权限");
        Map<String, List<PermissionVO>> grouped = permissionService.getPermissionsGrouped();
        return Result.success(grouped);
    }
}