package com.xtax.interceptor;

import com.xtax.annotation.Logical;
import com.xtax.annotation.RequirePermission;
import com.xtax.service.AuthService;
import com.xtax.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * 权限拦截器
 * 在请求处理前校验用户权限
 */
@Slf4j
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Autowired
    private AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果不是映射到方法，直接通过
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequirePermission annotation = handlerMethod.getMethodAnnotation(RequirePermission.class);
        
        // 如果方法上没有注解，检查类上是否有注解
        if (annotation == null) {
            annotation = handlerMethod.getBeanType().getAnnotation(RequirePermission.class);
        }
        
        // 如果没有权限注解，直接通过
        if (annotation == null) {
            return true;
        }

        // 从请求头获取token并解析用户ID
        // 支持从 Authorization: Bearer xxx 和 token: xxx 两种方式获取
        String authHeader = request.getHeader("Authorization");
        String token = request.getHeader("token");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        if (token == null || token.isEmpty()) {
            log.info("token不存在，返回401");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录\",\"data\":null}");
            return false;
        }

        try {
            Map<String, Object> claims = JwtUtils.parseToken(token);
            Integer userId = (Integer) claims.get("id");
            
            // 获取用户权限
            Set<String> userPermissions = authService.getUserPermissionCodes(userId);
            
            // 校验权限
            String[] requiredPermissions = annotation.value();
            Logical logical = annotation.logical();

            // 如果权限数组为空，直接放行
            if (requiredPermissions.length == 0) {
                log.info("权限数组为空，直接放行");
                return true;
            }

            boolean hasPermission = false;
            if (logical == Logical.AND) {
                // 需要同时拥有所有权限
                hasPermission = Arrays.stream(requiredPermissions)
                        .allMatch(userPermissions::contains);
            } else {
                // 拥有任意一个权限即可
                hasPermission = Arrays.stream(requiredPermissions)
                        .anyMatch(userPermissions::contains);
            }
            
            if (!hasPermission) {
                log.info("权限不足，返回403");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":403,\"message\":\"权限不足\",\"data\":null}");
                return false;
            }
            
            log.info("权限校验通过，放行");
            return true;
        } catch (Exception e) {
            log.error("解析token失败", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"token无效\",\"data\":null}");
            return false;
        }
    }
}