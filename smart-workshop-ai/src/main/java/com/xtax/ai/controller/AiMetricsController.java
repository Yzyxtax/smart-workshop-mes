package com.xtax.ai.controller;

import com.xtax.ai.agent.ToolRegistry;
import com.xtax.ai.service.AiMetricsService;
import com.xtax.vo.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * AI 指标查询控制器。
 * 提供 AI 模块的运行指标和状态监控。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@Slf4j
@RestController
@RequestMapping("/ai/metrics")
@RequiredArgsConstructor
public class AiMetricsController {

    private final AiMetricsService metricsService;
    private final ToolRegistry toolRegistry;

    /**
     * 获取 AI 模块运行指标快照
     * GET /ai/metrics
     */
    @GetMapping
    public Result getMetrics() {
        Map<String, Object> snapshot = metricsService.getMetricsSnapshot();

        // 附加工具注册信息
        Map<String, Object> result = new HashMap<>(snapshot);
        result.put("registeredTools", toolRegistry.getToolCount());
        result.put("toolNames", toolRegistry.getToolNames());

        return Result.success(result);
    }
}
