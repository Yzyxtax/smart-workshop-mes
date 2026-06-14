package com.xtax.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 分配权限DTO
 * 用于封装为角色分配权限的请求参数
 */
@Data
public class AssignPermissionDTO {
    @NotNull(message = "角色ID不能为空")
    private Integer roleId;
    @NotEmpty(message = "权限列表不能为空")
    private List<Integer> permissionIds;
}