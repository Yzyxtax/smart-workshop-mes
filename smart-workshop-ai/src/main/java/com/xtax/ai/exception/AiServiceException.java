package com.xtax.ai.exception;

/**
 * AI 服务异常。
 * LLM API 调用失败、超时等场景使用。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
public class AiServiceException extends RuntimeException {

    public AiServiceException(String message) {
        super(message);
    }

    public AiServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
