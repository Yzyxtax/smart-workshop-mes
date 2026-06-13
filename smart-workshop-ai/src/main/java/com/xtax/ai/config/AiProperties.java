package com.xtax.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 模块配置属性。
 * 绑定 application-ai.yml 中的 ai.* 配置项。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    /** LLM 提供商配置 */
    private LlmConfig llm = new LlmConfig();

    /** 会话配置 */
    private SessionConfig session = new SessionConfig();

    /** 限流配置 */
    private RateLimitConfig rateLimit = new RateLimitConfig();

    /** 消息配置 */
    private MessageConfig message = new MessageConfig();

    @Data
    public static class LlmConfig {
        /** 提供商标识：claude | openai | custom */
        private String provider = "claude";
        /** API 密钥 */
        private String apiKey;
        /** 模型名称 */
        private String model = "claude-opus-4-8";
        /** API 基础 URL */
        private String baseUrl = "https://api.anthropic.com";
        /** TCP 连接超时 */
        private String connectTimeout = "10s";
        /** 响应读取超时 */
        private String readTimeout = "60s";
        /** 单次最大输出 token */
        private Integer maxTokens = 4096;
        /** 生成温度 */
        private Double temperature = 0.3;
        /** 失效重试次数 */
        private Integer maxRetries = 3;
        /** 首次重试延迟（毫秒） */
        private Long retryBackoffBaseMs = 1000L;
    }

    @Data
    public static class SessionConfig {
        /** 上下文窗口历史轮数 */
        private Integer maxHistoryRounds = 20;
        /** 会话数据保留天数 */
        private Integer retentionDays = 90;
        /** 是否启用自动归档 */
        private Boolean archiveEnabled = false;
    }

    @Data
    public static class RateLimitConfig {
        /** 每分钟最大工具调用次数 */
        private Integer maxToolCallsPerMinute = 10;
    }

    @Data
    public static class MessageConfig {
        /** 用户输入最大字符数 */
        private Integer maxInputLength = 4000;
    }
}
