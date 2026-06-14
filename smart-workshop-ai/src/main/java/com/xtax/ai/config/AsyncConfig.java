package com.xtax.ai.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * AI 模块异步执行配置。
 * <p>
 * 启用 @Async 注解，并提供 AI Agent 专用线程池（aiTaskExecutor）。
 * <p>
 * 设计目的：
 * - 让 AiAgentServiceImpl.process() 在独立线程上执行，
 *   使 Controller 能立刻 return SseEmitter，
 *   让 Spring 把 emitter 关联到 HTTP 响应后再开始推送事件，
 *   从而实现真正的"边产生边推送"流式效果（生产者-消费者解耦）。
 *
 * @author AI Module Refactor
 * @since 2026-06-14
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * AI 任务专用线程池。
     * <p>
     * 与 Spring 默认 SimpleAsyncTaskExecutor 区别：
     * - 复用线程，避免每次新建线程的开销
     * - 队列 + 拒绝策略，避免线程数无限增长
     * - 名称前缀清晰，便于日志/线程栈排查
     *
     * @return AI 任务执行器
     */
    @Bean(name = "aiTaskExecutor")
    public Executor aiTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数：保持常驻，应对常规并发
        executor.setCorePoolSize(4);
        // 最大线程数：高峰期临时扩容
        executor.setMaxPoolSize(16);
        // 队列容量：超出核心线程数的任务先入队
        executor.setQueueCapacity(64);
        // 空闲线程存活时间（秒）
        executor.setKeepAliveSeconds(60);
        // 线程名前缀，方便日志/排障
        executor.setThreadNamePrefix("ai-agent-");
        // 拒绝策略：调用者线程执行（保守且不丢任务）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 关闭应用时等待任务完成，避免请求中途被打断
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        log.info("AI 任务线程池初始化完成: core=4, max=16, queue=64");
        return executor;
    }
}
