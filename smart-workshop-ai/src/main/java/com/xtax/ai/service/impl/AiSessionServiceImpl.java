package com.xtax.ai.service.impl;

import com.xtax.ai.entity.AiChatMessage;
import com.xtax.ai.entity.AiChatSession;
import com.xtax.ai.enums.MessageRole;
import com.xtax.ai.enums.SessionStatus;
import com.xtax.ai.mapper.AiChatMessageMapper;
import com.xtax.ai.mapper.AiChatSessionMapper;
import com.xtax.ai.service.AiSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * AI 会话管理服务实现
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiSessionServiceImpl implements AiSessionService {

    private final AiChatSessionMapper sessionMapper;
    private final AiChatMessageMapper messageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiChatSession createSession(Integer userId, String title) {
        AiChatSession session = new AiChatSession();
        session.setUserId(userId);
        session.setTitle(title != null ? title : "");
        session.setStatus(SessionStatus.ACTIVE.getCode());
        sessionMapper.insert(session);
        log.info("创建 AI 会话: id={}, userId={}", session.getId(), userId);
        return session;
    }

    @Override
    public List<AiChatSession> listSessions(Integer userId) {
        return sessionMapper.selectByUserId(userId);
    }

    @Override
    public AiChatSession getSession(Long sessionId) {
        return sessionMapper.selectById(sessionId);
    }

    @Override
    public AiChatSession getSessionWithMessages(Long sessionId) {
        return sessionMapper.selectById(sessionId);
    }

    @Override
    public List<AiChatMessage> getSessionMessages(Long sessionId) {
        return messageMapper.selectAllBySessionId(sessionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSession(Long sessionId) {
        sessionMapper.deleteById(sessionId);
        log.info("删除 AI 会话: id={}", sessionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiChatMessage saveUserMessage(Long sessionId, String content) {
        AiChatMessage message = new AiChatMessage();
        message.setSessionId(sessionId);
        message.setRole(MessageRole.USER.getCode());
        message.setContent(content);
        messageMapper.insert(message);
        log.debug("保存用户消息: sessionId={}, msgId={}", sessionId, message.getId());
        return message;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiChatMessage saveAssistantMessage(Long sessionId, String content,
                                               String toolCalls, String tokenUsage) {
        AiChatMessage message = new AiChatMessage();
        message.setSessionId(sessionId);
        message.setRole(MessageRole.ASSISTANT.getCode());
        message.setContent(content);
        message.setToolCalls(toolCalls);
        message.setTokenUsage(tokenUsage);
        messageMapper.insert(message);
        log.debug("保存 AI 回复: sessionId={}, msgId={}", sessionId, message.getId());
        return message;
    }

    @Override
    public void touchSession(Long sessionId) {
        sessionMapper.touchUpdateTime(sessionId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoGenerateTitle(Long sessionId, String content) {
        // 仅为首条用户消息自动生成标题
        int userMsgCount = messageMapper.countUserMessages(sessionId);
        if (userMsgCount == 1 && content != null && !content.isEmpty()) {
            String title = content.length() > 50 ? content.substring(0, 50) : content;
            // 清理换行符
            title = title.replace("\n", " ").replace("\r", " ").trim();
            sessionMapper.updateTitle(sessionId, title);
            log.debug("自动生成会话标题: sessionId={}, title={}", sessionId, title);
        }
    }
}
