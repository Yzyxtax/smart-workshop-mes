package com.xtax.vo;

import lombok.Data;

/**
 * 权限视图对象
 * 用于封装权限信息
 */
@Data
public class PermissionVO {
    private Integer id;
    private String permissionCode;
    private String permissionName;
    private String module;
    private String description;
}