package com.xtax.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 权限实体类
 * 用于封装权限信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Permission {
    private Integer id;
    private String permissionCode;
    private String permissionName;
    private String module;
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}