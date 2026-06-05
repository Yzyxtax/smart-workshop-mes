package com.xtax.annotation;

/**
 * 权限逻辑枚举
 * 用于定义权限校验的逻辑关系
 */
public enum Logical {
    /**
     * 需要同时拥有所有权限
     */
    AND,
    
    /**
     * 拥有任意一个权限即可
     */
    OR
}