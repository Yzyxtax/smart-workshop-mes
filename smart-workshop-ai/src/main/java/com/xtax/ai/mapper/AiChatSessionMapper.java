package com.xtax.ai.mapper;

import com.xtax.ai.entity.AiChatSession;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * AI 对话会话 Mapper 接口
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@Mapper
public interface AiChatSessionMapper {

    /**
     * 创建会话
     */
    @Insert("INSERT INTO ai_chat_session (user_id, title, status, created_at, updated_at) " +
            "VALUES (#{userId}, #{title}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    int insert(AiChatSession session);

    /**
     * 根据 ID 查询会话
     */
    @Select("SELECT id, user_id, title, status, created_at, updated_at " +
            "FROM ai_chat_session WHERE id = #{id}")
    AiChatSession selectById(Long id);

    /**
     * 分页查询用户会话列表（按活跃时间倒序）
     */
    @Select("SELECT id, user_id, title, status, created_at, updated_at " +
            "FROM ai_chat_session WHERE user_id = #{userId} AND status = 'ACTIVE' " +
            "ORDER BY updated_at DESC")
    List<AiChatSession> selectByUserId(@Param("userId") Integer userId);

    /**
     * 更新会话标题
     */
    @Update("UPDATE ai_chat_session SET title = #{title} WHERE id = #{id}")
    int updateTitle(@Param("id") Long id, @Param("title") String title);

    /**
     * 更新会话活跃时间
     */
    @Update("UPDATE ai_chat_session SET updated_at = NOW() WHERE id = #{id}")
    int touchUpdateTime(@Param("id") Long id);

    /**
     * 归档会话
     */
    @Update("UPDATE ai_chat_session SET status = 'ARCHIVED' WHERE id = #{id}")
    int archive(@Param("id") Long id);

    /**
     * 删除会话（数据库 ON DELETE CASCADE 级联删除消息）
     */
    @Delete("DELETE FROM ai_chat_session WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * 清理过期会话（超过保留天数）
     */
    @Delete("DELETE FROM ai_chat_session WHERE updated_at < DATE_SUB(NOW(), INTERVAL #{days} DAY)")
    int deleteExpiredSessions(@Param("days") Integer days);
}
