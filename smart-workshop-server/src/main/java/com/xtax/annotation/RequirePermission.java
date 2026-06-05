package com.xtax.annotation;

import java.lang.annotation.*;

/**
 * 权限注解
 * 用于标记需要权限校验的接口
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
    /**
     * 权限编码数组
     */
    String[] value() default {};
    
    /**
     * 逻辑关系：AND表示需要同时拥有所有权限，OR表示拥有任意一个权限即可
     */
    Logical logical() default Logical.AND;
}