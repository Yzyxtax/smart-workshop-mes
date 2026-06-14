package com.xtax.ai.agent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtax.ai.config.AiProperties;
import com.xtax.ai.dto.SendMessageRequest;
import com.xtax.ai.entity.AiChatMessage;
import com.xtax.ai.enums.MessageRole;
import com.xtax.ai.enums.SseEventType;
import com.xtax.ai.exception.AiRateLimitException;
import com.xtax.ai.exception.AiServiceException;
import com.xtax.ai.mapper.AiChatMessageMapper;
import com.xtax.ai.service.AiMetricsService;
import com.xtax.ai.service.AiSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.*;

/**
 * AI Agent 调度器实现。
 * 负责一次 AI 对话的完整生命周期管理，是整个 AI 模块的"大脑"。
 * <p>
 * 流程：组装上下文 → 调用 LLM → 解析工具调用 → 执行 → 返回结果 → 写入审计
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiAgentServiceImpl implements AiAgentService {

    private final AiChatMessageMapper messageMapper;
    private final AiSessionService sessionService;
    private final AiProvider aiProvider;
    private final ToolRegistry toolRegistry;
    private final ToolExecutor toolExecutor;
    private final AiMetricsService metricsService;
    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;

    /** 最大工具调用轮数 */
    private static final int MAX_TOOL_ROUNDS = 5;

    /** 确认语义关键词 */
    private static final Set<String> CONFIRM_KEYWORDS = Set.of(
            "确认", "是", "确定", "好的", "继续", "执行", "可以", "行", "好", "ok", "yes", "confirm"
    );

    @Override
    @Async("aiTaskExecutor")
    public void process(Long sessionId, SendMessageRequest request, SseEmitter emitter) {
        long startTime = System.currentTimeMillis();
        try {
            // ===== Phase 1: 准备 =====
            AiChatMessage userMsg = sessionService.saveUserMessage(sessionId, request.getContent());
            sessionService.autoGenerateTitle(sessionId, request.getContent());
            sessionService.touchSession(sessionId);

            // 获取当前用户信息（已由 Controller 在父线程中预提取并放入 request）
            Integer userId = request.getUserId() != null ? request.getUserId() : 0;
            String userName = request.getUserName() != null ? request.getUserName() : "未知用户";

            // ===== Phase 2: 组装上下文 =====
            List<Map<String, Object>> messages = buildContextMessages(sessionId, userId);
            List<Map<String, Object>> tools = toolRegistry.getToolDefinitions();

            // ===== Phase 3: LLM 推理循环（最多 5 轮工具调用） =====
            String fullResponse = "";
            Map<String, Object> tokenUsage = null;
            List<Map<String, Object>> toolCallRecords = new ArrayList<>();

            for (int round = 0; round < MAX_TOOL_ROUNDS; round++) {
                // 发送思考状态
                sendSseEvent(emitter, SseEventType.THINKING, Map.of());

                ChatRequest chatReq = ChatRequest.builder()
                        .messages(messages)
                        .tools(tools)
                        .maxTokens(aiProperties.getLlm().getMaxTokens())
                        .temperature(aiProperties.getLlm().getTemperature())
                        .model(aiProperties.getLlm().getModel())
                        .stream(true)
                        .build();

                // 流式调用 LLM
                Flux<AiEvent> eventFlux = aiProvider.streamChat(chatReq);
                List<AiEvent> events = eventFlux.collectList().block();

                if (events == null || events.isEmpty()) {
                    sendSseEvent(emitter, SseEventType.ERROR,
                            Map.of("code", "LLM_EMPTY", "message", "AI 服务返回空响应，请重试"));
                    emitter.complete();
                    return;
                }

                // 处理事件流：推送文本增量 + 收集完整响应
                boolean hasToolUse = false;
                for (AiEvent event : events) {
                    if (event.getType() == AiEvent.EventType.TEXT_DELTA) {
                        String delta = event.getData() != null ? event.getData().toString() : "";
                        fullResponse += delta;
                        sendSseEvent(emitter, SseEventType.TEXT_DELTA, Map.of("content", delta));
                    } else if (event.getType() == AiEvent.EventType.TOOL_USE) {
                        hasToolUse = true;

                        // ✅ 关键修复：在追加 role=tool 结果消息之前，
                        // 必须先把 assistant 自己的 tool_calls 消息追加到上下文。
                        // 这是 OpenAI / DeepSeek 协议的强制要求，否则下一轮会 400 Bad Request。
                        List<Map<String, Object>> toolCallsForAssistant = new ArrayList<>();
                        for (AiEvent.ToolUse tu : event.getToolUses()) {
                            Map<String, Object> functionPart = new LinkedHashMap<>();
                            functionPart.put("name", tu.getName());
                            // OpenAI 协议要求 arguments 是 JSON 字符串，不是对象
                            functionPart.put("arguments", toJson(tu.getParams()));

                            Map<String, Object> tcEntry = new LinkedHashMap<>();
                            tcEntry.put("id", tu.getId());
                            tcEntry.put("type", "function");
                            tcEntry.put("function", functionPart);
                            toolCallsForAssistant.add(tcEntry);
                        }
                        Map<String, Object> assistantToolCallMsg = new LinkedHashMap<>();
                        assistantToolCallMsg.put("role", "assistant");
                        // content 必须存在，可以为空串；许多兼容实现不接受 null
                        assistantToolCallMsg.put("content", "");
                        assistantToolCallMsg.put("tool_calls", toolCallsForAssistant);
                        messages.add(assistantToolCallMsg);

                        // 执行每一个工具调用
                        for (AiEvent.ToolUse toolUse : event.getToolUses()) {
                            sendSseEvent(emitter, SseEventType.TOOL_CALL, Map.of(
                                    "name", toolUse.getName(),
                                    "params", toolUse.getParams()
                            ));

                            // 构建执行上下文
                            ToolContext ctx = ToolContext.builder()
                                    .userId(userId)
                                    .userName(userName)
                                    .sessionId(sessionId)
                                    .messageId(userMsg.getId())
                                    .userNaturalInput(request.getContent())
                                    .confirmed(detectConfirmation(messages))
                                    .build();

                            ToolResult result = toolExecutor.execute(
                                    toolUse.getName(), toolUse.getParams(), ctx);

                            sendSseEvent(emitter, SseEventType.TOOL_RESULT,
                                    Map.of("tool", toolUse.getName(), "result", result.toMap()));

                            // 将工具结果追加到消息上下文
                            messages.add(Map.of(
                                    "role", "tool",
                                    "tool_call_id", toolUse.getId(),
                                    "content", toJson(result.toMap())
                            ));

                            toolCallRecords.add(Map.of(
                                    "name", toolUse.getName(),
                                    "params", toolUse.getParams(),
                                    "result", Map.of("success", result.isSuccess())
                            ));

                            // 如果需要确认，不再继续本轮其他工具调用
                            if (result.isNeedsConfirmation()) {
                                break;
                            }
                        }
                    } else if (event.getType() == AiEvent.EventType.TEXT_COMPLETE) {
                        tokenUsage = event.getTokenUsage();
                    }
                }

                // 如果没有工具调用，说明 LLM 已给出最终文本回复
                if (!hasToolUse) {
                    break;
                }
            }

            // ===== Phase 4: 保存 AI 回复并收尾 =====
            String toolCallsJson = toolCallRecords.isEmpty() ? null : toJson(toolCallRecords);
            String tokenUsageJson = tokenUsage != null ? toJson(tokenUsage) : null;

            AiChatMessage assistantMsg = sessionService.saveAssistantMessage(
                    sessionId, fullResponse, toolCallsJson, tokenUsageJson);

            // 记录指标
            long latency = System.currentTimeMillis() - startTime;
            if (tokenUsage != null) {
                Object promptTokens = tokenUsage.getOrDefault("prompt_tokens",
                        tokenUsage.getOrDefault("input_tokens", 0));
                Object completionTokens = tokenUsage.getOrDefault("completion_tokens",
                        tokenUsage.getOrDefault("output_tokens", 0));
                metricsService.recordLlmSuccess(latency, 0,
                        toLong(promptTokens), toLong(completionTokens));
            }

            sendSseEvent(emitter, SseEventType.DONE, Map.of(
                    "messageId", assistantMsg.getId(),
                    "tokenUsage", tokenUsage != null ? tokenUsage : Map.of()
            ));
            emitter.complete();

        } catch (AiRateLimitException e) {
            sendSseEvent(emitter, SseEventType.ERROR,
                    Map.of("code", "RATE_LIMIT", "message", e.getMessage()));
            emitter.complete();
            metricsService.recordLlmFailure("RATE_LIMIT");
        } catch (AiServiceException e) {
            sendSseEvent(emitter, SseEventType.ERROR,
                    Map.of("code", "LLM_UNAVAILABLE", "message", "AI 服务暂不可用，请稍后重试"));
            emitter.complete();
            metricsService.recordLlmFailure("LLM_UNAVAILABLE");
        } catch (Exception e) {
            log.error("AI Agent processing failed: sessionId={}", sessionId, e);
            sendSseEvent(emitter, SseEventType.ERROR,
                    Map.of("code", "INTERNAL_ERROR", "message", "AI 处理出错，请重试"));
            emitter.complete();
            metricsService.recordLlmFailure("INTERNAL_ERROR");
        }
    }

    // ========== 上下文组装 ==========

    /**
     * 构建发送给 LLM 的完整消息数组
     */
    private List<Map<String, Object>> buildContextMessages(Long sessionId, Integer userId) {
        List<Map<String, Object>> messages = new ArrayList<>();

        // 1. System Prompt（始终第一条）
        messages.add(Map.of("role", "system", "content", buildSystemPrompt(userId)));

        // 2. 历史消息（最近 N 轮）
        int maxHistory = aiProperties.getSession().getMaxHistoryRounds();
        List<AiChatMessage> history = messageMapper.selectRecentBySessionId(sessionId, maxHistory * 2);
        for (AiChatMessage msg : history) {
            String role = msg.getRole();
            // 跳过 tool 角色消息（LLM 上下文中不需要）
            if (MessageRole.TOOL.getCode().equals(role)) {
                continue;
            }
            messages.add(Map.of("role", role, "content", msg.getContent()));
        }

        return messages;
    }

    /**
     * 构建 System Prompt
     */
    private String buildSystemPrompt(Integer userId) {
        return """
                你是智能车间 MES 系统的 AI 助手。你可以：
                1. 回答用户关于生产管理、设备、BOM、工艺流程等方面的问题
                2. 根据用户指令，调用系统工具完成数据查询和操作
                3. 在执行写入操作前，清楚地说明即将执行的操作并获得确认

                重要约束：
                - 始终以中文回复
                - 对于删除、终止等破坏性操作，必须获得用户明确确认后再执行
                - 当用户请求模糊时，主动追问澄清
                - 工具调用结果中的技术错误信息，应转化为用户友好的自然语言
                - 当前用户信息已注入上下文，根据用户角色调整回复风格
                - 当前用户 ID: """ + userId + """
                """;
    }

    // ========== 确认检测 ==========

    /**
     * 检测上一条用户消息是否包含确认语义
     */
    private boolean detectConfirmation(List<Map<String, Object>> messages) {
        // 从后往前找最近的 user 消息
        for (int i = messages.size() - 1; i >= 0; i--) {
            Map<String, Object> msg = messages.get(i);
            if ("user".equals(msg.get("role"))) {
                String content = msg.get("content") != null ? msg.get("content").toString().toLowerCase() : "";
                return CONFIRM_KEYWORDS.stream().anyMatch(content::contains);
            }
        }
        return false;
    }

    // ========== 用户信息提取 ==========
    // 注意：因 process() 已改为 @Async 异步执行，子线程无法访问 RequestContextHolder，
    // 用户信息（userId / userName）改由 Controller 在父线程中提取后通过 SendMessageRequest 传入。

    // ========== 辅助方法 ==========

    /**
     * 安全发送 SSE 事件
     */
    private void sendSseEvent(SseEmitter emitter, SseEventType eventType, Object data) {
        try {
            emitter.send(eventType.toEvent(data));
        } catch (IOException e) {
            log.warn("SSE 事件发送失败: eventType={}", eventType.getEventName());
        }
    }

    /**
     * 对象转 JSON（安全，不抛异常）
     */
    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return String.valueOf(obj);
        }
    }

    /**
     * Object 转 long
     */
    private long toLong(Object obj) {
        if (obj == null) return 0;
        if (obj instanceof Number) return ((Number) obj).longValue();
        try {
            return Long.parseLong(obj.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
