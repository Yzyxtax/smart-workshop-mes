package com.xtax.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 分配角色DTO
 * 用于封装为用户分配角色的请求参数
 */
@Data
public class AssignRoleDTO {
    @NotNull(message = "用户ID不能为空")
    private Integer userId;
    @NotEmpty(message = "角色列表不能为空")
    private List<Integer> roleIds;
}