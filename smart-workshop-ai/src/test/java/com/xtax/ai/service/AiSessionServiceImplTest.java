package com.xtax.ai.service;

import com.xtax.ai.entity.AiChatMessage;
import com.xtax.ai.entity.AiChatSession;
import com.xtax.ai.enums.MessageRole;
import com.xtax.ai.enums.SessionStatus;
import com.xtax.ai.mapper.AiChatMessageMapper;
import com.xtax.ai.mapper.AiChatSessionMapper;
import com.xtax.ai.service.impl.AiSessionServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * AiSessionServiceImpl 单元测试。
 * 验证会话和消息的 CRUD、标题自动生成等逻辑。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@DisplayName("AiSessionService 会话管理测试")
@ExtendWith(MockitoExtension.class)
class AiSessionServiceImplTest {

    @Mock
    private AiChatSessionMapper sessionMapper;

    @Mock
    private AiChatMessageMapper messageMapper;

    @InjectMocks
    private AiSessionServiceImpl sessionService;

    // ==================== createSession ====================

    @Nested
    @DisplayName("createSession")
    class CreateSession {

        @Test
        @DisplayName("应正确设置 userId 和状态为 ACTIVE")
        void shouldSetUserIdAndActiveStatus() {
            when(sessionMapper.insert(any(AiChatSession.class))).thenAnswer(inv -> {
                AiChatSession s = inv.getArgument(0);
                s.setId(1L);
                return 1;
            });

            AiChatSession session = sessionService.createSession(1001, "测试会话");

            assertNotNull(session.getId());
            assertEquals(1001, session.getUserId());
            assertEquals(SessionStatus.ACTIVE.getCode(), session.getStatus());
        }

        @Test
        @DisplayName("title 为 null 时应设为空字符串")
        void shouldDefaultNullTitleToEmpty() {
            when(sessionMapper.insert(any(AiChatSession.class))).thenAnswer(inv -> {
                AiChatSession s = inv.getArgument(0);
                s.setId(2L);
                return 1;
            });

            AiChatSession session = sessionService.createSession(1001, null);

            assertEquals("", session.getTitle());
        }

        @Test
        @DisplayName("title 不为 null 时应正确设置")
        void shouldSetGivenTitle() {
            when(sessionMapper.insert(any(AiChatSession.class))).thenAnswer(inv -> {
                AiChatSession s = inv.getArgument(0);
                s.setId(3L);
                return 1;
            });

            AiChatSession session = sessionService.createSession(1001, "生产计划查询");

            assertEquals("生产计划查询", session.getTitle());
        }
    }

    // ==================== saveUserMessage ====================

    @Nested
    @DisplayName("saveUserMessage")
    class SaveUserMessage {

        @Test
        @DisplayName("角色应设为 USER")
        void shouldSetRoleToUser() {
            when(messageMapper.insert(any(AiChatMessage.class))).thenAnswer(inv -> {
                AiChatMessage m = inv.getArgument(0);
                m.setId(10L);
                return 1;
            });

            AiChatMessage msg = sessionService.saveUserMessage(1L, "帮我查询设备状态");

            assertEquals(MessageRole.USER.getCode(), msg.getRole());
            assertEquals(1L, msg.getSessionId());
            assertEquals("帮我查询设备状态", msg.getContent());
        }
    }

    // ==================== saveAssistantMessage ====================

    @Nested
    @DisplayName("saveAssistantMessage")
    class SaveAssistantMessage {

        @Test
        @DisplayName("角色应设为 ASSISTANT 并保存工具调用和 token 信息")
        void shouldSetAssistantFields() {
            when(messageMapper.insert(any(AiChatMessage.class))).thenAnswer(inv -> {
                AiChatMessage m = inv.getArgument(0);
                m.setId(20L);
                return 1;
            });

            AiChatMessage msg = sessionService.saveAssistantMessage(
                    1L, "查询到 3 台设备", "[{\"tool\":\"search_equipment\"}]", "{\"input\":100,\"output\":50}");

            assertEquals(MessageRole.ASSISTANT.getCode(), msg.getRole());
            assertEquals("查询到 3 台设备", msg.getContent());
            assertEquals("[{\"tool\":\"search_equipment\"}]", msg.getToolCalls());
            assertEquals("{\"input\":100,\"output\":50}", msg.getTokenUsage());
        }
    }

    // ==================== autoGenerateTitle ====================

    @Nested
    @DisplayName("autoGenerateTitle")
    class AutoGenerateTitle {

        @Test
        @DisplayName("首个用户消息且短于 50 字符时应使用完整内容作为标题")
        void shouldUseFullContentWhenFirstMsgShort() {
            when(messageMapper.countUserMessages(1L)).thenReturn(1);

            sessionService.autoGenerateTitle(1L, "帮我查询一号产线的设备状态");

            verify(sessionMapper).updateTitle(eq(1L), eq("帮我查询一号产线的设备状态"));
        }

        @Test
        @DisplayName("首个用户消息且超过 50 字符时应截断为 50 字符")
        void shouldTruncateLongContent() {
            String longContent = "这是一个非常非常长的消息内容用于测试标题截断功能是否正常工作我们需要确保系统正确处理超长内容截断为五十个字符";
            when(messageMapper.countUserMessages(1L)).thenReturn(1);

            sessionService.autoGenerateTitle(1L, longContent);

            ArgumentCaptor<String> titleCaptor = ArgumentCaptor.forClass(String.class);
            verify(sessionMapper).updateTitle(eq(1L), titleCaptor.capture());
            assertTrue(titleCaptor.getValue().length() <= 50);
            assertEquals(longContent.substring(0, 50), titleCaptor.getValue());
        }

        @Test
        @DisplayName("非首个消息时不应更新标题")
        void shouldNotUpdateTitleWhenNotFirstMessage() {
            when(messageMapper.countUserMessages(1L)).thenReturn(3);

            sessionService.autoGenerateTitle(1L, "第三条消息");

            verify(sessionMapper, never()).updateTitle(anyLong(), anyString());
        }

        @Test
        @DisplayName("第 0 条消息时不应更新标题（边界情况）")
        void shouldNotUpdateTitleWhenZeroMessages() {
            when(messageMapper.countUserMessages(1L)).thenReturn(0);

            sessionService.autoGenerateTitle(1L, "某条消息");

            verify(sessionMapper, never()).updateTitle(anyLong(), anyString());
        }

        @Test
        @DisplayName("消息内容包含换行符时应替换为空格")
        void shouldReplaceNewlinesWithSpaces() {
            when(messageMapper.countUserMessages(1L)).thenReturn(1);

            sessionService.autoGenerateTitle(1L, "查询产线状态\n\n并生成报告\r\n确认");

            ArgumentCaptor<String> titleCaptor = ArgumentCaptor.forClass(String.class);
            verify(sessionMapper).updateTitle(eq(1L), titleCaptor.capture());
            assertThat(titleCaptor.getValue()).doesNotContain("\n");
            assertThat(titleCaptor.getValue()).doesNotContain("\r");
            assertThat(titleCaptor.getValue()).contains("并生成报告");
        }

        @Test
        @DisplayName("null 内容不应更新标题")
        void shouldNotUpdateTitleForNullContent() {
            when(messageMapper.countUserMessages(1L)).thenReturn(1);

            sessionService.autoGenerateTitle(1L, null);

            verify(sessionMapper, never()).updateTitle(anyLong(), anyString());
        }

        @Test
        @DisplayName("空字符串内容不应更新标题")
        void shouldNotUpdateTitleForEmptyContent() {
            when(messageMapper.countUserMessages(1L)).thenReturn(1);

            sessionService.autoGenerateTitle(1L, "");

            verify(sessionMapper, never()).updateTitle(anyLong(), anyString());
        }
    }

    // ==================== 委托方法 ====================

    @Nested
    @DisplayName("Mapper 委托方法")
    class MapperDelegation {

        @Test
        @DisplayName("listSessions 应委托给 sessionMapper.selectByUserId")
        void shouldDelegateListSessions() {
            AiChatSession s1 = new AiChatSession();
            s1.setId(1L);
            s1.setTitle("测试");
            when(sessionMapper.selectByUserId(1001)).thenReturn(List.of(s1));

            List<AiChatSession> result = sessionService.listSessions(1001);

            assertEquals(1, result.size());
            verify(sessionMapper).selectByUserId(1001);
        }

        @Test
        @DisplayName("getSession 应委托给 sessionMapper.selectById")
        void shouldDelegateGetSession() {
            AiChatSession session = new AiChatSession();
            session.setId(1L);
            when(sessionMapper.selectById(1L)).thenReturn(session);

            AiChatSession result = sessionService.getSession(1L);

            assertSame(session, result);
        }

        @Test
        @DisplayName("getSessionMessages 应委托给 messageMapper.selectAllBySessionId")
        void shouldDelegateGetMessages() {
            AiChatMessage m1 = new AiChatMessage();
            m1.setId(1L);
            when(messageMapper.selectAllBySessionId(1L)).thenReturn(List.of(m1));

            List<AiChatMessage> result = sessionService.getSessionMessages(1L);

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("touchSession 应委托给 sessionMapper.touchUpdateTime")
        void shouldDelegateTouchSession() {
            sessionService.touchSession(5L);

            verify(sessionMapper).touchUpdateTime(5L);
        }

        @Test
        @DisplayName("deleteSession 应委托给 sessionMapper.deleteById")
        void shouldDelegateDeleteSession() {
            sessionService.deleteSession(5L);

            verify(sessionMapper).deleteById(5L);
        }
    }
}
