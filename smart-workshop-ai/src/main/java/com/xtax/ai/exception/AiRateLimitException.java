package com.xtax.ai.exception;

/**
 * AI 模块限流异常。
 * 当同一会话的工具调用超过频率限制时抛出。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
public class AiRateLimitException extends RuntimeException {

    public AiRateLimitException(String message) {
        super(message);
    }

    public AiRateLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}
