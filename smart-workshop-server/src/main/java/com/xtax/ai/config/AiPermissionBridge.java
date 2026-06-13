package com.xtax.ai.config;

import com.xtax.ai.agent.ToolExecutor;
import com.xtax.service.AuthService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * AI 模块权限桥接配置。
 * 将 server 模块的 AuthService 注入到 ai 模块的 ToolExecutor 中，
 * 解决模块间编译依赖隔离问题。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiPermissionBridge {

    private final ToolExecutor toolExecutor;
    private final AuthService authService;

    /**
     * 启动时将 AuthService 作为 PermissionChecker 注入 ToolExecutor
     */
    @PostConstruct
    public void init() {
        toolExecutor.setPermissionChecker(authService::getUserPermissionCodes);
        log.info("AI 模块权限桥接完成：AuthService → ToolExecutor.PermissionChecker");
    }
}
