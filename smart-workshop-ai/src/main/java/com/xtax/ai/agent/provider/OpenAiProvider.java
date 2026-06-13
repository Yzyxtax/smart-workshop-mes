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
 * OpenAI API 实现。
 * 通过 OpenAI Chat Completions API 的 SSE 流式端点与 GPT 模型交互。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiProvider implements AiProvider {

    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(String provider) {
        return "openai".equalsIgnoreCase(provider);
    }

    @Override
    public Flux<AiEvent> streamChat(ChatRequest request) {
        return Flux.create(sink -> {
            try {
                Map<String, Object> body = buildOpenAiRequestBody(request);
                String apiKey = aiProperties.getLlm().getApiKey();
                String baseUrl = aiProperties.getLlm().getBaseUrl();

                if (apiKey == null || apiKey.isEmpty()) {
                    sink.error(new AiServiceException("OpenAI API Key 未配置，请设置 AI_API_KEY 环境变量"));
                    return;
                }

                WebClient client = WebClient.builder()
                        .baseUrl(baseUrl)
                        .defaultHeader("Authorization", "Bearer " + apiKey)
                        .build();

                StringBuilder textBuffer = new StringBuilder();
                List<AiEvent.ToolUse> toolUses = new ArrayList<>();
                Map<Integer, Map<String, Object>> toolUseBuilders = new HashMap<>();
                Map<Integer, StringBuilder> toolArgBuilders = new HashMap<>();
                Map<String, Object> usage = null;

                String responseBody = client.post()
                        .uri("/v1/chat/completions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(body)
                        .accept(MediaType.TEXT_EVENT_STREAM)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                if (responseBody == null) {
                    sink.error(new AiServiceException("OpenAI API 返回空响应"));
                    return;
                }

                // 解析 SSE 流
                String[] lines = responseBody.split("\n");
                for (String line : lines) {
                    if (!line.startsWith("data: ")) continue;

                    String json = line.substring(6).trim();
                    if (json.isEmpty() || "[DONE]".equals(json)) continue;

                    try {
                        Map<String, Object> event = objectMapper.readValue(json,
                                new TypeReference<Map<String, Object>>() {});
                        List<Map<String, Object>> choices = (List<Map<String, Object>>) event.get("choices");
                        if (choices == null || choices.isEmpty()) continue;

                        Map<String, Object> choice = choices.get(0);
                        Map<String, Object> delta = (Map<String, Object>) choice.get("delta");

                        if (delta != null) {
                            // 文本增量
                            Object content = delta.get("content");
                            if (content != null && !content.toString().isEmpty()) {
                                String text = content.toString();
                                textBuffer.append(text);
                                sink.next(AiEvent.builder()
                                        .type(AiEvent.EventType.TEXT_DELTA)
                                        .data(text)
                                        .build());
                            }

                            // 工具调用增量
                            List<Map<String, Object>> toolCalls = (List<Map<String, Object>>) delta.get("tool_calls");
                            if (toolCalls != null) {
                                for (Map<String, Object> tc : toolCalls) {
                                    Integer index = (Integer) tc.get("index");
                                    if (index == null) continue;

                                    // 初始化 builder
                                    toolUseBuilders.putIfAbsent(index, new HashMap<>());
                                    toolArgBuilders.putIfAbsent(index, new StringBuilder());

                                    String tcId = (String) tc.get("id");
                                    if (tcId != null) {
                                        toolUseBuilders.get(index).put("id", tcId);
                                    }

                                    Map<String, Object> function = (Map<String, Object>) tc.get("function");
                                    if (function != null) {
                                        String funcName = (String) function.get("name");
                                        if (funcName != null) {
                                            toolUseBuilders.get(index).put("name", funcName);
                                        }
                                        Object args = function.get("arguments");
                                        if (args != null) {
                                            toolArgBuilders.get(index).append(args.toString());
                                        }
                                    }
                                }
                            }
                        }

                        // 结束原因
                        String finishReason = (String) choice.get("finish_reason");
                        if (finishReason != null) {
                            // 收尾工具调用
                            for (Map.Entry<Integer, Map<String, Object>> entry : toolUseBuilders.entrySet()) {
                                Integer idx = entry.getKey();
                                Map<String, Object> builder = entry.getValue();
                                StringBuilder argsBuilder = toolArgBuilders.get(idx);

                                if (builder.containsKey("name") && argsBuilder != null
                                        && argsBuilder.length() > 0) {
                                    try {
                                        Map<String, Object> params = objectMapper.readValue(
                                                argsBuilder.toString(),
                                                new TypeReference<Map<String, Object>>() {});
                                        toolUses.add(AiEvent.ToolUse.builder()
                                                .id((String) builder.getOrDefault("id", "call_" + idx))
                                                .name((String) builder.get("name"))
                                                .params(params)
                                                .build());
                                    } catch (Exception e) {
                                        log.warn("解析 OpenAI 工具参数失败: {}", argsBuilder, e);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("解析 OpenAI SSE 事件失败: {}", json, e);
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
                log.error("OpenAI API 调用失败", e);
                sink.error(new AiServiceException("AI 服务调用失败: " + e.getMessage(), e));
            }
        });
    }

    /**
     * 构建 OpenAI Chat Completions API 请求体。
     * 将通用消息和工具格式转换为 OpenAI 格式。
     */
    private Map<String, Object> buildOpenAiRequestBody(ChatRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", aiProperties.getLlm().getModel());
        body.put("max_tokens", request.getMaxTokens() != null ? request.getMaxTokens() : 4096);
        body.put("temperature", request.getTemperature() != null ? request.getTemperature() : 0.3);

        // 转换消息格式（OpenAI 与通用格式基本兼容）
        body.put("messages", request.getMessages());

        // 转换工具格式
        if (request.getTools() != null && !request.getTools().isEmpty()) {
            List<Map<String, Object>> tools = request.getTools().stream()
                    .map(t -> {
                        Map<String, Object> tool = new LinkedHashMap<>();
                        tool.put("type", "function");
                        Map<String, Object> function = new LinkedHashMap<>();
                        function.put("name", t.get("name"));
                        function.put("description", t.get("description"));
                        Map<String, Object> params = new LinkedHashMap<>();
                        params.put("type", "object");
                        params.put("properties", t.get("input_schema") instanceof Map
                                ? ((Map<String, Object>) t.get("input_schema")).get("properties")
                                : Map.of());
                        Object required = t.get("input_schema") instanceof Map
                                ? ((Map<String, Object>) t.get("input_schema")).get("required")
                                : null;
                        if (required != null) {
                            params.put("required", required);
                        }
                        function.put("parameters", params);
                        tool.put("function", function);
                        return tool;
                    })
                    .collect(Collectors.toList());
            body.put("tools", tools);
            body.put("tool_choice", "auto");
        }

        body.put("stream", true);
        return body;
    }
}
