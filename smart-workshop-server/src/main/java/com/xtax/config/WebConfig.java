package com.xtax.config;

import com.xtax.interceptor.PermissionInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 用于注册拦截器
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private PermissionInterceptor permissionInterceptor;

    /**
     * 注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册权限拦截器
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/**")  // 拦截所有路径
                .excludePathPatterns(    // 排除不需要权限校验的路径
                        "/login",
                        "/error",
                        "/permission/**",  // 权限查询接口不需要权限校验
                        "/role/**",        // 角色查询接口暂时不需要权限校验（可根据需求调整）
                        "/auth/**"         // 权限校验接口本身不需要权限校验
                );
    }
}