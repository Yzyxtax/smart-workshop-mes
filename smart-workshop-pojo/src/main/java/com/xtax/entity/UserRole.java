package com.xtax.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户角色关联实体类
 * 用于封装用户与角色的关联关系
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRole {
    private Integer id;
    private Integer userId;
    private Integer roleId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}