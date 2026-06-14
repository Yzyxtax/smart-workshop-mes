package com.xtax.controller;

import com.xtax.annotation.RequirePermission;
import com.xtax.dto.AssignRoleDTO;
import com.xtax.service.UserRoleService;
import com.xtax.vo.RoleVO;
import com.xtax.vo.Result;
import com.xtax.vo.UserVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户角色分配控制器
 * 提供用户角色分配相关的REST接口
 */
@Slf4j
@RestController
@RequestMapping("/user-role")
public class UserRoleController {

    @Autowired
    private UserRoleService userRoleService;

    /**
     * 查询用户的角色列表
     */
    @GetMapping("/user/{userId}")
    public Result getRolesByUserId(@PathVariable Integer userId) {
        log.info("查询用户角色，userId：{}", userId);
        List<RoleVO> roles = userRoleService.getRolesByUserId(userId);
        return Result.success(roles);
    }

    /**
     * 查询角色下的用户列表
     */
    @GetMapping("/role/{roleId}")
    public Result getUsersByRoleId(@PathVariable Integer roleId) {
        log.info("查询角色用户，roleId：{}", roleId);
        List<UserVO> users = userRoleService.getUsersByRoleId(roleId);
        return Result.success(users);
    }

    /**
     * 为用户分配角色
     */
    @PostMapping
    @RequirePermission("SYS_PERMISSION_ASSIGN")
    public Result assignRoles(@Valid @RequestBody AssignRoleDTO dto) {
        log.info("为用户分配角色：{}", dto);
        int result = userRoleService.assignRoles(dto);
        if (result > 0) {
            return Result.success("分配成功");
        }
        return Result.success("无新增角色分配");
    }

    /**
     * 移除用户角色
     */
    @DeleteMapping
    @RequirePermission("SYS_PERMISSION_ASSIGN")
    public Result removeRoles(@RequestParam Integer userId, @RequestParam List<Integer> roleIds) {
        log.info("移除用户角色，userId：{}，roleIds：{}", userId, roleIds);
        // 空列表校验：避免生成非法SQL
        if (roleIds == null || roleIds.isEmpty()) {
            return Result.error(400, "请选择要移除的角色");
        }
        int result = userRoleService.removeRoles(userId, roleIds);
        if (result > 0) {
            return Result.success("移除成功");
        }
        return Result.success("关联不存在，无需移除");
    }
}