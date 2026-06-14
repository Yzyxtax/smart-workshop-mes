package com.xtax.vo;

import lombok.Data;

import java.util.List;

/**
 * 用户权限视图对象
 * 用于封装用户的所有权限信息，包含角色和权限编码列表
 */
@Data
public class UserPermissionVO {
    private Integer userId;
    private String username;
    private String name;
    private List<RoleVO> roles;
    private List<String> permissions;
}