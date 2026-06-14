package com.xtax.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 角色查询参数DTO
 * 用于封装角色查询的条件参数
 */
@Data
public class RoleQueryParam {
    @Min(value = 1, message = "页码必须大于0")
    private Integer page;
    @Min(value = 1, message = "每页条数必须大于0")
    private Integer pageSize;
    private String roleName;

    /**
     * 计算分页偏移量
     * @return OFFSET 值，page 或 pageSize 为 null 时返回 null
     */
    public Integer getOffset() {
        if (page != null && pageSize != null && page > 0 && pageSize > 0) {
            return (page - 1) * pageSize;
        }
        return null;
    }
}