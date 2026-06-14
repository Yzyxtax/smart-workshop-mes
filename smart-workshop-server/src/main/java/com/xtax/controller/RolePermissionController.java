package com.xtax.controller;

import com.xtax.annotation.RequirePermission;
import com.xtax.dto.AssignPermissionDTO;
import com.xtax.service.RolePermissionService;
import com.xtax.vo.PermissionVO;
import com.xtax.vo.RoleVO;
import com.xtax.vo.Result;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色权限分配控制器
 * 提供角色权限分配相关的REST接口
 */
@Slf4j
@RestController
@RequestMapping("/role-permission")
public class RolePermissionController {

    @Autowired
    private RolePermissionService rolePermissionService;

    /**
     * 查询角色的权限列表
     */
    @GetMapping("/role/{roleId}")
    public Result getPermissionsByRoleId(@PathVariable Integer roleId) {
        log.info("查询角色权限，roleId：{}", roleId);
        List<PermissionVO> permissions = rolePermissionService.getPermissionsByRoleId(roleId);
        return Result.success(permissions);
    }

    /**
     * 查询权限所属的角色列表
     */
    @GetMapping("/permission/{permissionId}")
    public Result getRolesByPermissionId(@PathVariable Integer permissionId) {
        log.info("查询权限角色，permissionId：{}", permissionId);
        List<RoleVO> roles = rolePermissionService.getRolesByPermissionId(permissionId);
        return Result.success(roles);
    }

    /**
     * 为角色分配权限
     */
    @PostMapping
    @RequirePermission("SYS_PERMISSION_ASSIGN")
    public Result assignPermissions(@Valid @RequestBody AssignPermissionDTO dto) {
        log.info("为角色分配权限：{}", dto);
        int result = rolePermissionService.assignPermissions(dto);
        if (result > 0) {
            return Result.success("分配成功");
        }
        return Result.success("无新增权限分配");
    }

    /**
     * 移除角色权限
     */
    @DeleteMapping
    @RequirePermission("SYS_PERMISSION_ASSIGN")
    public Result removePermissions(@RequestParam Integer roleId, @RequestParam List<Integer> permissionIds) {
        log.info("移除角色权限，roleId：{}，permissionIds：{}", roleId, permissionIds);
        // 空列表校验：避免生成非法SQL
        if (permissionIds == null || permissionIds.isEmpty()) {
            return Result.error(400, "请选择要移除的权限");
        }
        int result = rolePermissionService.removePermissions(roleId, permissionIds);
        if (result > 0) {
            return Result.success("移除成功");
        }
        // 语义对齐 UserRoleController：关联不存在时返回友好提示
        return Result.success("关联不存在，无需移除");
    }
}