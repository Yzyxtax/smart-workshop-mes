package com.xtax.controller;

import com.xtax.service.AuthService;
import com.xtax.utils.JwtUtils;
import com.xtax.vo.Result;
import com.xtax.vo.UserPermissionVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 权限校验控制器
 * 提供权限校验相关的REST接口
 */
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 查询当前用户权限
     */
    @GetMapping("/permissions")
    public Result getCurrentUserPermissions(HttpServletRequest request, HttpServletResponse response) {
        log.info("查询当前用户权限");
        // 从请求头获取token并解析用户ID（支持 Authorization: Bearer 和 token 两种方式）
        String token = extractToken(request);
        if (token == null || token.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return Result.error(401, "未登录");
        }

        try {
            Map<String, Object> claims = JwtUtils.parseToken(token);
            Integer userId = (Integer) claims.get("id");
            UserPermissionVO permissions = authService.getCurrentUserPermissions(userId);
            return Result.success(permissions);
        } catch (Exception e) {
            log.error("解析token失败", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return Result.error(401, "token无效");
        }
    }

    /**
     * 校验用户权限
     */
    @GetMapping("/check")
    public Result checkPermission(HttpServletRequest request, HttpServletResponse response, @RequestParam String permissionCode) {
        log.info("校验用户权限，permissionCode：{}", permissionCode);
        // 从请求头获取token并解析用户ID（支持 Authorization: Bearer 和 token 两种方式）
        String token = extractToken(request);
        if (token == null || token.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return Result.error(401, "未登录");
        }

        try {
            Map<String, Object> claims = JwtUtils.parseToken(token);
            Integer userId = (Integer) claims.get("id");
            boolean hasPermission = authService.checkPermission(userId, permissionCode);

            Map<String, Object> result = new HashMap<>();
            result.put("hasPermission", hasPermission);
            return Result.success(result);
        } catch (Exception e) {
            log.error("解析token失败", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return Result.error(401, "token无效");
        }
    }

    /**
     * 从请求中提取token（支持 Authorization: Bearer xxx 和 token: xxx 两种方式）
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = request.getHeader("token");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        return token;
    }
}