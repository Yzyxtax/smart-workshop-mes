package com.xtax.ai.tool.team;

import com.xtax.ai.agent.ToolContext;
import com.xtax.ai.agent.ToolResult;
import com.xtax.entity.TeamItem;
import com.xtax.service.teamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * SearchTeamTool 单元测试。
 * 验证班组关键词搜索的过滤逻辑。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@DisplayName("SearchTeamTool 搜索班组测试")
@ExtendWith(MockitoExtension.class)
class SearchTeamToolTest {

    @Mock
    private teamService teamService;

    @InjectMocks
    private SearchTeamTool tool;

    private final ToolContext ctx = ToolContext.builder()
            .userId(1001).sessionId(1L).messageId(10L).build();

    @BeforeEach
    void setUp() {
        tool = new SearchTeamTool(teamService);
    }

    @Test
    @DisplayName("按班组名称关键词搜索应返回匹配结果")
    void shouldSearchByTeamName() {
        when(teamService.getAllTeam()).thenReturn(List.of(
                new TeamItem("T01", "组装一班"),
                new TeamItem("T02", "组装二班"),
                new TeamItem("T03", "焊接班")
        ));

        ToolResult result = tool.execute(Map.of("keyword", "组装"), ctx);

        assertTrue(result.isSuccess());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(2, data.get("count"));
    }

    @Test
    @DisplayName("按班组编号关键词搜索应返回匹配结果")
    void shouldSearchByTeamNo() {
        when(teamService.getAllTeam()).thenReturn(List.of(
                new TeamItem("T01", "组装一班"),
                new TeamItem("T02", "组装二班")
        ));

        ToolResult result = tool.execute(Map.of("keyword", "T01"), ctx);

        assertTrue(result.isSuccess());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(1, data.get("count"));
    }

    @Test
    @DisplayName("无匹配关键词时应返回空列表")
    void shouldReturnEmptyWhenNoMatch() {
        when(teamService.getAllTeam()).thenReturn(List.of(
                new TeamItem("T01", "组装一班")
        ));

        ToolResult result = tool.execute(Map.of("keyword", "不存在"), ctx);

        assertTrue(result.isSuccess());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(0, data.get("count"));
        assertThat((List<?>) data.get("teams")).isEmpty();
    }

    @Test
    @DisplayName("数据库中无班组时应返回空列表")
    void shouldReturnEmptyWhenNoData() {
        when(teamService.getAllTeam()).thenReturn(Collections.emptyList());

        ToolResult result = tool.execute(Map.of("keyword", "组装"), ctx);

        assertTrue(result.isSuccess());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(0, data.get("count"));
    }

    @Test
    @DisplayName("null 班组列表应安全处理")
    void shouldHandleNullTeamList() {
        when(teamService.getAllTeam()).thenReturn(null);

        ToolResult result = tool.execute(Map.of("keyword", "组装"), ctx);

        assertTrue(result.isSuccess());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(0, data.get("count"));
    }

    @Test
    @DisplayName("班组名称为 null 时应安全处理")
    void shouldHandleNullTeamName() {
        when(teamService.getAllTeam()).thenReturn(List.of(
                new TeamItem("T01", null),
                new TeamItem("T02", "组装二班")
        ));

        ToolResult result = tool.execute(Map.of("keyword", "组装"), ctx);

        assertTrue(result.isSuccess());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(1, data.get("count"));
    }
}
