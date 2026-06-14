package com.xtax.ai.agent;

import com.xtax.ai.config.AiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AI 提供商工厂。
 * 根据配置 ai.llm.provider 的值，从所有已注册的 AiProvider 实现中选择匹配的提供商。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiProviderFactory {

    private final List<AiProvider> providers;
    private final AiProperties aiProperties;

    /**
     * 根据配置选择并返回对应的 AiProvider Bean
     */
    @Bean
    @Primary
    public AiProvider aiProvider() {
        String providerName = aiProperties.getLlm().getProvider();
        return providers.stream()
                .filter(p -> p.supports(providerName))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("不支持的 LLM 提供商: {}，可用提供商: {}",
                            providerName,
                            providers.stream().map(p -> p.getClass().getSimpleName()).toList());
                    return new IllegalStateException("不支持的 LLM 提供商: " + providerName
                            + "，请检查 ai.llm.provider 配置");
                });
    }
}
