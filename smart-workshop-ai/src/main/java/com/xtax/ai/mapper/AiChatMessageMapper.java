package com.xtax.ai.mapper;

import com.xtax.ai.entity.AiChatMessage;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * AI 对话消息 Mapper 接口
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@Mapper
public interface AiChatMessageMapper {

    /**
     * 保存消息
     */
    @Insert("INSERT INTO ai_chat_message (session_id, role, content, tool_calls, token_usage, created_at) " +
            "VALUES (#{sessionId}, #{role}, #{content}, #{toolCalls}, #{tokenUsage}, NOW())")
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    int insert(AiChatMessage message);

    /**
     * 查询会话的最近 N 轮消息（按时间正序）
     */
    @Select("SELECT id, session_id, role, content, tool_calls, token_usage, created_at " +
            "FROM ai_chat_message WHERE session_id = #{sessionId} " +
            "ORDER BY created_at ASC LIMIT #{limit}")
    List<AiChatMessage> selectRecentBySessionId(@Param("sessionId") Long sessionId,
                                                 @Param("limit") Integer limit);

    /**
     * 查询会话的全部消息（按时间正序）
     */
    @Select("SELECT id, session_id, role, content, tool_calls, token_usage, created_at " +
            "FROM ai_chat_message WHERE session_id = #{sessionId} " +
            "ORDER BY created_at ASC")
    List<AiChatMessage> selectAllBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 查询会话中 user 角色的消息数量（用于判断是否首条消息）
     */
    @Select("SELECT COUNT(*) FROM ai_chat_message WHERE session_id = #{sessionId} AND role = 'user'")
    int countUserMessages(@Param("sessionId") Long sessionId);

    /**
     * 查询会话中最近一条 user 角色的消息
     */
    @Select("SELECT id, session_id, role, content, tool_calls, token_usage, created_at " +
            "FROM ai_chat_message WHERE session_id = #{sessionId} AND role = 'user' " +
            "ORDER BY created_at DESC LIMIT 1")
    AiChatMessage selectLastUserMessage(@Param("sessionId") Long sessionId);
}
