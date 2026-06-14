package com.xtax.filter;

import com.xtax.utils.JwtUtils;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@WebFilter(urlPatterns = "/*")
public class LoginFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        log.info("进入过滤器LoginFilter");
        //1. 获取请求的uri
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String requestURI = request.getRequestURI();

        //2. 判断是否是登录请求
        if (requestURI.contains("/login")) {
            log.info("登录请求，放行");
            filterChain.doFilter(request, response);
            return;
        }

        // 3. 获取请求头中的token (支持从 Authorization: Bearer xxx 和 token: xxx 两种方式获取)
        String authHeader = request.getHeader("Authorization");
        String token = request.getHeader("token"); // 保留原来的兼容性

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // 去掉 "Bearer " 前缀
        }

        //4. 判断token是否存在
        if (token == null || token.isEmpty()) {
            log.info("token不存在，返回401");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        //5. 验证token
        try {
            JwtUtils.parseToken(token);
        } catch(Exception e){
            log.info("token验证失败，返回401");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        //6. 放行
        log.info("token验证成功，放行");
        filterChain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
