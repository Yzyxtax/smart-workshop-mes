package com.xtax.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 角色实体类
 * 用于封装角色信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    private Integer id;
    @NotBlank(message = "角色编码不能为空")
    private String roleCode;
    @NotBlank(message = "角色名称不能为空")
    private String roleName;
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}