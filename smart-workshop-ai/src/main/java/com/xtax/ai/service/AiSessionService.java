package com.xtax.ai.service;

import com.xtax.ai.entity.AiChatMessage;
import com.xtax.ai.entity.AiChatSession;

import java.util.List;

/**
 * AI 会话管理服务接口
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
public interface AiSessionService {

    /**
     * 创建新会话
     *
     * @param userId 用户 ID
     * @param title  会话标题（可选）
     * @return 创建的会话
     */
    AiChatSession createSession(Integer userId, String title);

    /**
     * 查询用户的会话列表
     *
     * @param userId 用户 ID
     * @return 会话列表（按活跃时间倒序）
     */
    List<AiChatSession> listSessions(Integer userId);

    /**
     * 查询会话详情
     *
     * @param sessionId 会话 ID
     * @return 会话实体
     */
    AiChatSession getSession(Long sessionId);

    /**
     * 查询会话详情及全部消息历史
     *
     * @param sessionId 会话 ID
     * @return 会话实体
     */
    AiChatSession getSessionWithMessages(Long sessionId);

    /**
     * 查询会话全部消息（按时间正序）
     *
     * @param sessionId 会话 ID
     * @return 消息列表
     */
    List<AiChatMessage> getSessionMessages(Long sessionId);

    /**
     * 删除会话（级联删除消息）
     *
     * @param sessionId 会话 ID
     */
    void deleteSession(Long sessionId);

    /**
     * 保存用户消息
     *
     * @param sessionId 会话 ID
     * @param content   消息内容
     * @return 保存的消息
     */
    AiChatMessage saveUserMessage(Long sessionId, String content);

    /**
     * 保存 AI 回复消息
     *
     * @param sessionId  会话 ID
     * @param content    回复内容
     * @param toolCalls  工具调用记录
     * @param tokenUsage token 消耗
     * @return 保存的消息
     */
    AiChatMessage saveAssistantMessage(Long sessionId, String content,
                                        String toolCalls, String tokenUsage);

    /**
     * 更新会话活跃时间
     *
     * @param sessionId 会话 ID
     */
    void touchSession(Long sessionId);

    /**
     * 自动生成会话标题（基于首条用户消息前 50 字符）
     *
     * @param sessionId 会话 ID
     * @param content   首条消息内容
     */
    void autoGenerateTitle(Long sessionId, String content);
}
