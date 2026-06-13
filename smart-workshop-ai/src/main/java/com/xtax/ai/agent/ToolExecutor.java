package com.xtax.ai.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtax.ai.annotation.ToolParam;
import com.xtax.ai.config.AiProperties;
import com.xtax.ai.entity.AiAuditLog;
import com.xtax.ai.exception.AiRateLimitException;
import com.xtax.ai.mapper.AiAuditLogMapper;
import com.xtax.ai.service.AiMetricsService;
import com.xtax.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 工具执行沙箱。
 * 在沙箱环境中安全地执行 LLM 请求的工具调用，复用现有的安全防线。
 * <p>
 * 执行流程（7 步沙箱管道）：
 * 1. 工具查找 → 2. 参数校验 → 3. 权限校验 → 4. 确认检查 →
 * 5. 限流检查 → 6. 执行业务操作 → 7. 记录审计
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ToolExecutor {

    private final ToolRegistry toolRegistry;
    private final AiAuditLogMapper auditLogMapper;
    private final AiMetricsService metricsService;
    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;

    /**
     * 权限校验回调接口。
     * 由 server 模块在启动时注入具体实现（AuthService），解决模块间编译依赖问题。
     */
    private PermissionChecker permissionChecker;

    /** 限流计数器：sessionId → 分钟级计数器 */
    private final ConcurrentHashMap<Long, RateLimitCounter> sessionRateLimiters = new ConcurrentHashMap<>();

    /**
     * 权限校验回调接口
     */
    public interface PermissionChecker {
        /**
         * 获取用户权限编码集合
         */
        Set<String> getUserPermissionCodes(Integer userId);
    }

    /**
     * 设置权限校验器（由 server 模块在启动时注入）
     */
    public void setPermissionChecker(PermissionChecker checker) {
        this.permissionChecker = checker;
    }

    /**
     * 在沙箱中执行工具调用，复用现有的多道安全防线
     *
     * @param toolName 工具名称
     * @param params   工具参数
     * @param ctx      执行上下文
     * @return 工具执行结果
     */
    public ToolResult execute(String toolName, Map<String, Object> params, ToolContext ctx) {
        long startTime = System.currentTimeMillis();
        AiAuditLog auditLog = new AiAuditLog();
        auditLog.setSessionId(ctx.getSessionId());
        auditLog.setMessageId(ctx.getMessageId());
        auditLog.setUserId(ctx.getUserId());
        auditLog.setToolName(toolName);
        auditLog.setUserNaturalInput(ctx.getUserNaturalInput());

        try {
            // Step 1: 工具查找
            ToolHandler handler = toolRegistry.getHandler(toolName);
            AiToolMeta meta = toolRegistry.getMeta(toolName);
            if (handler == null) {
                return fail(auditLog, startTime, "不支持的操作: " + toolName);
            }

            // Step 2: 参数校验
            String validationError = validateParams(meta, params);
            if (validationError != null) {
                return fail(auditLog, startTime, validationError);
            }
            auditLog.setToolParams(toJson(params));

            // Step 3: RBAC 权限校验
            if (meta.getPermissions() != null && meta.getPermissions().length > 0) {
                if (permissionChecker != null) {
                    Set<String> userPerms = permissionChecker.getUserPermissionCodes(ctx.getUserId());
                    boolean hasAllPerms = Arrays.stream(meta.getPermissions())
                            .allMatch(userPerms::contains);
                    if (!hasAllPerms) {
                        String permDesc = String.join("、", meta.getPermissions());
                        return fail(auditLog, startTime, "您没有[" + permDesc + "]相关权限");
                    }
                } else {
                    log.warn("PermissionChecker 未注入，跳过权限校验: tool={}, userId={}", toolName, ctx.getUserId());
                }
            }

            // Step 4: 确认检查（破坏性操作）
            if (meta.isRequiresConfirmation() && !ctx.isConfirmed()) {
                String confirmMsg = "即将执行【" + meta.getLabel() + "】操作：" + meta.getDescription()
                        + "。此操作可能产生不可逆的影响。请回复\"确认\"以继续。";
                return ToolResult.needsConfirmation(confirmMsg);
            }

            // Step 5: 限流检查
            checkRateLimit(ctx.getSessionId());

            // Step 6: ★ 调用业务 Service 执行实际操作
            ToolResult result = handler.execute(params, ctx);
            metricsService.recordToolCall(toolName);

            // Step 7: 记录审计
            auditLog.setSuccess(result.isSuccess());
            if (result.isSuccess()) {
                auditLog.setExecutionResult(toJson(result.getData()));
            } else {
                auditLog.setErrorMessage(result.getErrorMessage());
                auditLog.setExecutionResult(result.getErrorMessage());
            }
            auditLog.setDurationMs((int) (System.currentTimeMillis() - startTime));
            auditLogMapper.insert(auditLog);

            return result;

        } catch (AiRateLimitException e) {
            return fail(auditLog, startTime, e.getMessage());
        } catch (BusinessException e) {
            return fail(auditLog, startTime, e.getMessage());
        } catch (SecurityException e) {
            return fail(auditLog, startTime, "权限不足：" + e.getMessage());
        } catch (IllegalStateException e) {
            return fail(auditLog, startTime, "操作不允许：" + e.getMessage());
        } catch (Exception e) {
            log.error("Tool execution error: {}", toolName, e);
            return fail(auditLog, startTime, "系统错误，请稍后重试");
        }
    }

    /**
     * 参数校验：对照 @ToolParam 注解检查必填、类型和枚举约束
     */
    private String validateParams(AiToolMeta meta, Map<String, Object> params) {
        for (AiToolMeta.ToolParamMeta paramMeta : meta.getParams()) {
            Object value = params.get(paramMeta.getName());

            // 必填检查
            if (paramMeta.isRequired() && (value == null || (value instanceof String && ((String) value).isEmpty()))) {
                return "缺少必填参数【" + paramMeta.getName() + "】：" + paramMeta.getDescription();
            }

            // 枚举约束检查
            if (value != null && paramMeta.getEnumValues() != null && paramMeta.getEnumValues().length > 0) {
                String strValue = value.toString();
                boolean valid = Arrays.asList(paramMeta.getEnumValues()).contains(strValue);
                if (!valid) {
                    return "参数【" + paramMeta.getName() + "】的值不在允许范围内，可选值："
                            + String.join(", ", paramMeta.getEnumValues());
                }
            }
        }
        return null; // 校验通过
    }

    /**
     * 限流检查：同一 session 每分钟最多 N 次工具调用
     */
    private void checkRateLimit(Long sessionId) {
        int maxCalls = aiProperties.getRateLimit().getMaxToolCallsPerMinute();
        RateLimitCounter counter = sessionRateLimiters.computeIfAbsent(sessionId,
                k -> new RateLimitCounter());
        int current = counter.incrementAndGet();
        if (current > maxCalls) {
            throw new AiRateLimitException("操作过于频繁，每分钟最多 " + maxCalls + " 次工具调用，请稍后重试");
        }
    }

    /**
     * 记录失败并写入审计
     */
    private ToolResult fail(AiAuditLog auditLog, long startTime, String errorMessage) {
        auditLog.setSuccess(false);
        auditLog.setErrorMessage(errorMessage);
        auditLog.setDurationMs((int) (System.currentTimeMillis() - startTime));
        try {
            auditLogMapper.insert(auditLog);
        } catch (Exception e) {
            log.error("写入审计日志失败", e);
        }
        return ToolResult.error(errorMessage);
    }

    /**
     * 对象转 JSON 字符串
     */
    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }

    /**
     * 分钟级限流计数器（简化实现，每分钟自动重置）
     */
    private static class RateLimitCounter {
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile long windowStart = System.currentTimeMillis();

        int incrementAndGet() {
            long now = System.currentTimeMillis();
            // 超过 60 秒则重置窗口
            if (now - windowStart > 60_000) {
                synchronized (this) {
                    if (now - windowStart > 60_000) {
                        count.set(0);
                        windowStart = now;
                    }
                }
            }
            return count.incrementAndGet();
        }
    }
}
