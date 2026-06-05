package com.xtax.vo;

import lombok.Data;

import java.util.List;

/**
 * 角色视图对象
 * 用于封装角色详情信息，包含关联的权限列表
 */
@Data
public class RoleVO {
    private Integer id;
    private String roleCode;
    private String roleName;
    private String description;
    private List<PermissionVO> permissions;
    private Integer permissionCount;
}