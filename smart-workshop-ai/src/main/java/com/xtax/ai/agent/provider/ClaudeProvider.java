package com.xtax.ai.agent.provider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xtax.ai.agent.AiEvent;
import com.xtax.ai.agent.AiProvider;
import com.xtax.ai.agent.ChatRequest;
import com.xtax.ai.config.AiProperties;
import com.xtax.ai.exception.AiServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Claude (Anthropic) API 实现。
 * 通过 Anthropic Messages API 的 SSE 流式端点与 Claude 模型交互。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClaudeProvider implements AiProvider {

    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(String provider) {
        return "claude".equalsIgnoreCase(provider);
    }

    @Override
    public Flux<AiEvent> streamChat(ChatRequest request) {
        return Flux.create(sink -> {
            try {
                Map<String, Object> body = buildClaudeRequestBody(request);
                String apiKey = aiProperties.getLlm().getApiKey();
                String baseUrl = aiProperties.getLlm().getBaseUrl();

                if (apiKey == null || apiKey.isEmpty()) {
                    sink.error(new AiServiceException("Claude API Key 未配置，请设置 AI_API_KEY 环境变量"));
                    return;
                }

                WebClient client = WebClient.builder()
                        .baseUrl(baseUrl)
                        .defaultHeader("x-api-key", apiKey)
                        .defaultHeader("anthropic-version", "2023-06-01")
                        .build();

                StringBuilder textBuffer = new StringBuilder();
                List<AiEvent.ToolUse> toolUses = new ArrayList<>();
                Map<String, Object> currentToolUse = null;
                StringBuilder currentToolInput = new StringBuilder();
                Map<String, Object> usage = null;

                String responseBody = client.post()
                        .uri("/v1/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(body)
                        .accept(MediaType.TEXT_EVENT_STREAM)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                if (responseBody == null) {
                    sink.error(new AiServiceException("Claude API 返回空响应"));
                    return;
                }

                // 解析 SSE 流
                String[] lines = responseBody.split("\n");
                for (String line : lines) {
                    if (!line.startsWith("data: ")) continue;

                    String json = line.substring(6).trim();
                    if (json.isEmpty()) continue;

                    try {
                        Map<String, Object> event = objectMapper.readValue(json,
                                new TypeReference<Map<String, Object>>() {});
                        String eventType = (String) event.get("type");

                        switch (eventType) {
                            case "content_block_delta":
                                Map<String, Object> delta = (Map<String, Object>) event.get("delta");
                                if (delta == null) break;
                                String deltaType = (String) delta.get("type");
                                if ("text_delta".equals(deltaType)) {
                                    String text = (String) delta.get("text");
                                    if (text != null) {
                                        textBuffer.append(text);
                                        sink.next(AiEvent.builder()
                                                .type(AiEvent.EventType.TEXT_DELTA)
                                                .data(text)
                                                .build());
                                    }
                                } else if ("input_json_delta".equals(deltaType)) {
                                    String partialJson = (String) delta.get("partial_json");
                                    if (partialJson != null) {
                                        currentToolInput.append(partialJson);
                                    }
                                }
                                break;

                            case "content_block_start":
                                Map<String, Object> contentBlock = (Map<String, Object>) event.get("content_block");
                                if (contentBlock != null && "tool_use".equals(contentBlock.get("type"))) {
                                    currentToolUse = new HashMap<>();
                                    currentToolUse.put("id", contentBlock.get("id"));
                                    currentToolUse.put("name", contentBlock.get("name"));
                                    currentToolInput = new StringBuilder();
                                }
                                break;

                            case "content_block_stop":
                                if (currentToolUse != null && currentToolInput.length() > 0) {
                                    try {
                                        Map<String, Object> params = objectMapper.readValue(
                                                currentToolInput.toString(),
                                                new TypeReference<Map<String, Object>>() {});
                                        toolUses.add(AiEvent.ToolUse.builder()
                                                .id((String) currentToolUse.get("id"))
                                                .name((String) currentToolUse.get("name"))
                                                .params(params)
                                                .build());
                                    } catch (Exception e) {
                                        log.warn("解析工具参数 JSON 失败: {}", currentToolInput, e);
                                    }
                                    currentToolUse = null;
                                    currentToolInput = new StringBuilder();
                                }
                                break;

                            case "message_delta":
                                Map<String, Object> deltaUsage = (Map<String, Object>) event.get("usage");
                                if (deltaUsage != null) {
                                    if (usage == null) usage = new HashMap<>();
                                    usage.put("completion_tokens",
                                            (Integer) deltaUsage.getOrDefault("output_tokens", 0));
                                }
                                break;

                            case "message_stop":
                                // 消息结束
                                break;

                            case "error":
                                Map<String, Object> error = (Map<String, Object>) event.get("error");
                                String errorMsg = error != null ?
                                        (String) error.getOrDefault("message", "Claude API 错误") : "Claude API 错误";
                                sink.error(new AiServiceException(errorMsg));
                                return;
                        }
                    } catch (Exception e) {
                        log.warn("解析 Claude SSE 事件失败: {}", json, e);
                    }
                }

                // 发出最终事件
                if (!toolUses.isEmpty()) {
                    sink.next(AiEvent.builder()
                            .type(AiEvent.EventType.TOOL_USE)
                            .toolUses(toolUses)
                            .build());
                } else {
                    sink.next(AiEvent.builder()
                            .type(AiEvent.EventType.TEXT_COMPLETE)
                            .data(textBuffer.toString())
                            .tokenUsage(usage)
                            .build());
                }

                sink.complete();

            } catch (AiServiceException e) {
                sink.error(e);
            } catch (Exception e) {
                log.error("Claude API 调用失败", e);
                sink.error(new AiServiceException("AI 服务调用失败: " + e.getMessage(), e));
            }
        });
    }

    /**
     * 构建 Claude API 请求体。
     * 将通用消息格式转换为 Anthropic Messages API 格式。
     */
    private Map<String, Object> buildClaudeRequestBody(ChatRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", aiProperties.getLlm().getModel());
        body.put("max_tokens", request.getMaxTokens() != null ? request.getMaxTokens() : 4096);
        body.put("temperature", request.getTemperature() != null ? request.getTemperature() : 0.3);

        // 分离 system 消息和普通消息
        String systemPrompt = null;
        List<Map<String, Object>> messages = new ArrayList<>();

        for (Map<String, Object> msg : request.getMessages()) {
            String role = (String) msg.get("role");
            if ("system".equals(role)) {
                // Claude 需要将 system 消息放到顶层
                Object content = msg.get("content");
                if (content instanceof String) {
                    systemPrompt = (String) content;
                } else if (content instanceof List) {
                    // 处理 content 数组格式
                    List<Map<String, Object>> contentList = (List<Map<String, Object>>) content;
                    for (Map<String, Object> block : contentList) {
                        if ("text".equals(block.get("type"))) {
                            systemPrompt = (String) block.get("text");
                        }
                    }
                }
            } else {
                messages.add(convertMessage(msg));
            }
        }

        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            body.put("system", systemPrompt);
        }
        body.put("messages", messages);

        // 转换工具格式
        if (request.getTools() != null && !request.getTools().isEmpty()) {
            List<Map<String, Object>> tools = request.getTools().stream()
                    .map(t -> {
                        Map<String, Object> tool = new LinkedHashMap<>();
                        tool.put("name", t.get("name"));
                        tool.put("description", t.get("description"));
                        tool.put("input_schema", t.get("input_schema"));
                        return tool;
                    })
                    .collect(Collectors.toList());
            body.put("tools", tools);
        }

        body.put("stream", true);
        return body;
    }

    /**
     * 将通用消息格式转换为 Claude 消息格式
     */
    private Map<String, Object> convertMessage(Map<String, Object> msg) {
        Map<String, Object> converted = new LinkedHashMap<>();
        converted.put("role", msg.get("role"));
        Object content = msg.get("content");
        if (content instanceof String) {
            converted.put("content", content);
        } else {
            converted.put("content", content);
        }
        return converted;
    }
}
