package com.xtax.ai.tool.line;

import com.xtax.ai.agent.ToolContext;
import com.xtax.ai.agent.ToolResult;
import com.xtax.entity.ProductionLine;
import com.xtax.service.lineService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * SearchLineTool 单元测试。
 * 验证产线名称/编号模糊搜索。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@DisplayName("SearchLineTool 搜索产线测试")
@ExtendWith(MockitoExtension.class)
class SearchLineToolTest {

    @Mock
    private lineService lineService;

    @InjectMocks
    private SearchLineTool tool;

    private final ToolContext ctx = ToolContext.builder()
            .userId(1001).sessionId(1L).messageId(10L).build();

    @Test
    @DisplayName("按产线名称关键词搜索应返回匹配结果")
    void shouldSearchByLineName() {
        when(lineService.getAllLine()).thenReturn(List.of(
                new ProductionLine("L01", "一号产线", null, null, null),
                new ProductionLine("L02", "二号产线", null, null, null),
                new ProductionLine("L03", "焊接产线", null, null, null)
        ));

        ToolResult result = tool.execute(Map.of("keyword", "产线"), ctx);

        assertTrue(result.isSuccess());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(3, data.get("count"));
    }

    @Test
    @DisplayName("按产线编号部分匹配应返回结果")
    void shouldSearchByLineNo() {
        when(lineService.getAllLine()).thenReturn(List.of(
                new ProductionLine("L01", "一号产线", null, null, null),
                new ProductionLine("L02", "二号产线", null, null, null)
        ));

        ToolResult result = tool.execute(Map.of("keyword", "L0"), ctx);

        assertTrue(result.isSuccess());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(2, data.get("count"));
    }

    @Test
    @DisplayName("无匹配时应返回空列表")
    void shouldReturnEmptyWhenNoMatch() {
        when(lineService.getAllLine()).thenReturn(List.of(
                new ProductionLine("L01", "一号产线", null, null, null)
        ));

        ToolResult result = tool.execute(Map.of("keyword", "不存在"), ctx);

        assertTrue(result.isSuccess());
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) result.getData();
        assertEquals(0, data.get("count"));
    }

    @Test
    @DisplayName("空数据应安全返回空列表")
    void shouldHandleEmptyData() {
        when(lineService.getAllLine()).thenReturn(Collections.emptyList());

        ToolResult result = tool.execute(Map.of("keyword", "L01"), ctx);

        assertTrue(result.isSuccess());
    }
}
