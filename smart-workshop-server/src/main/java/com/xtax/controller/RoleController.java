package com.xtax.controller;

import com.xtax.dto.RoleQueryParam;
import com.xtax.entity.Role;
import com.xtax.service.RoleService;
import com.xtax.vo.RoleVO;
import com.xtax.vo.Result;
import com.xtax.vo.ResultPage;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 * 提供角色相关的REST接口
 */
@Slf4j
@RestController
@RequestMapping("/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * 查询角色列表
     */
    @GetMapping
    public Result getAllRole(@Valid RoleQueryParam param) {
        log.info("查询角色列表，参数：{}", param);
        ResultPage<Role> roles = roleService.getAllRole(param);
        if (roles != null) {
            return Result.success(roles);
        }
        return Result.error("查询失败");
    }

    /**
     * 查询角色详情
     */
    @GetMapping("/{id}")
    public Result getRoleById(@PathVariable Integer id) {
        log.info("查询角色详情，id：{}", id);
        RoleVO role = roleService.getRoleById(id);
        if (role != null) {
            return Result.success(role);
        }
        return Result.error("查询失败");
    }

    /**
     * 创建角色
     */
    @PostMapping
    public Result addRole(@Valid @RequestBody Role role) {
        log.info("创建角色：{}", role);
        int result = roleService.addRole(role);
        if (result > 0) {
            return Result.success("创建成功");
        }
        return Result.error("创建失败");
    }

    /**
     * 更新角色
     */
    @PutMapping
    public Result updateRole(@Valid @RequestBody Role role) {
        log.info("更新角色：{}", role);
        int result = roleService.updateRole(role);
        if (result > 0) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    /**
     * 删除角色
     */
    @DeleteMapping
    public Result deleteRoles(@RequestParam List<Integer> ids) {
        log.info("删除角色，ids：{}", ids);
        if (ids == null || ids.isEmpty()) {
            return Result.error(400, "请选择要删除的角色");
        }
        int result = roleService.deleteRoles(ids);
        if (result > 0) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}