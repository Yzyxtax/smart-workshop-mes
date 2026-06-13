package com.xtax.ai.service.impl;

import com.xtax.ai.service.AiMetricsService;
import com.xtax.ai.mapper.AiAuditLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AI 指标监控服务实现。
 * 使用 AtomicLong 和 ConcurrentHashMap 保证线程安全。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiMetricsServiceImpl implements AiMetricsService {

    private final AiAuditLogMapper auditLogMapper;

    // ========== 指标计数器（AtomicLong 保证线程安全） ==========

    /** 总请求数 */
    private final AtomicLong totalRequests = new AtomicLong(0);

    /** 成功数 */
    private final AtomicLong totalSuccesses = new AtomicLong(0);

    /** 失败数 */
    private final AtomicLong totalFailures = new AtomicLong(0);

    /** Prompt token 累计 */
    private final AtomicLong totalTokensPrompt = new AtomicLong(0);

    /** Completion token 累计 */
    private final AtomicLong totalTokensCompletion = new AtomicLong(0);

    /** 按工具统计调用次数 */
    private final ConcurrentHashMap<String, AtomicLong> toolCallCounts = new ConcurrentHashMap<>();

    /** 按天统计工具调用 */
    private final ConcurrentHashMap<String, AtomicLong> dailyToolCalls = new ConcurrentHashMap<>();

    /** 按错误类型统计 */
    private final ConcurrentHashMap<String, AtomicLong> errorCounts = new ConcurrentHashMap<>();

    /** 累计延迟 */
    private final AtomicLong totalLatencyMs = new AtomicLong(0);

    /** 累计 TTFT 延迟 */
    private final AtomicLong totalTtftMs = new AtomicLong(0);

    @Override
    public Map<String, Object> getMetricsSnapshot() {
        long total = totalRequests.get();
        long successes = totalSuccesses.get();

        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("totalRequests", total);
        snapshot.put("successRate", total > 0 ? (double) successes / total : 0.0);
        snapshot.put("avgLatencyMs", total > 0 ? totalLatencyMs.get() / total : 0);
        snapshot.put("avgTtftMs", total > 0 ? totalTtftMs.get() / total : 0);
        snapshot.put("totalTokensConsumed", totalTokensPrompt.get() + totalTokensCompletion.get());

        // 最近 7 天工具调用趋势
        List<Map<String, Object>> dailyTrend = auditLogMapper.countByDay(7);
        snapshot.put("toolCallsByDay", dailyTrend);

        // 工具调用分布
        Map<String, Long> toolBreakdown = new HashMap<>();
        toolCallCounts.forEach((k, v) -> toolBreakdown.put(k, v.get()));
        snapshot.put("toolCallBreakdown", toolBreakdown);

        // 运行时错误分布
        Map<String, Long> errBreakdown = new HashMap<>();
        errorCounts.forEach((k, v) -> errBreakdown.put(k, v.get()));
        snapshot.put("errorBreakdown", errBreakdown);

        return snapshot;
    }

    @Override
    public void recordLlmSuccess(long latencyMs, long ttftMs, long promptTokens, long completionTokens) {
        totalRequests.incrementAndGet();
        totalSuccesses.incrementAndGet();
        totalLatencyMs.addAndGet(latencyMs);
        totalTtftMs.addAndGet(ttftMs);
        totalTokensPrompt.addAndGet(promptTokens);
        totalTokensCompletion.addAndGet(completionTokens);
    }

    @Override
    public void recordLlmFailure(String errorType) {
        totalRequests.incrementAndGet();
        totalFailures.incrementAndGet();
        errorCounts.computeIfAbsent(errorType, k -> new AtomicLong()).incrementAndGet();
    }

    @Override
    public void recordToolCall(String toolName) {
        toolCallCounts.computeIfAbsent(toolName, k -> new AtomicLong()).incrementAndGet();
        String today = LocalDate.now().toString();
        dailyToolCalls.computeIfAbsent(today, k -> new AtomicLong()).incrementAndGet();
    }
}
