package com.xtax.ai.agent;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ToolResult 值对象单元测试。
 * 验证工厂方法和 toMap() 转换逻辑。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@DisplayName("ToolResult 值对象测试")
class ToolResultTest {

    // ==================== success() 工厂方法 ====================

    @Test
    @DisplayName("success(data) 应返回 success=true 且 data 完整的结果")
    void shouldCreateSuccessWithData() {
        Map<String, Object> data = Map.of("count", 3, "items", List.of("A", "B", "C"));

        ToolResult result = ToolResult.success(data);

        assertTrue(result.isSuccess());
        assertEquals(data, result.getData());
        assertNull(result.getErrorMessage());
        assertFalse(result.isNeedsConfirmation());
    }

    @Test
    @DisplayName("success() 无参工厂方法应返回 success=true、data=null 的结果")
    void shouldCreateSuccessWithoutData() {
        ToolResult result = ToolResult.success();

        assertTrue(result.isSuccess());
        assertNull(result.getData());
        assertNull(result.getErrorMessage());
    }

    // ==================== error() 工厂方法 ====================

    @Test
    @DisplayName("error(msg) 应返回 success=false 且包含错误信息的结果")
    void shouldCreateErrorWithMessage() {
        ToolResult result = ToolResult.error("操作失败：参数无效");

        assertFalse(result.isSuccess());
        assertEquals("操作失败：参数无效", result.getErrorMessage());
        assertNull(result.getData());
        assertFalse(result.isNeedsConfirmation());
    }

    // ==================== needsConfirmation() 工厂方法 ====================

    @Test
    @DisplayName("needsConfirmation(msg) 应返回 needsConfirmation=true 的结果")
    void shouldCreateConfirmationRequired() {
        ToolResult result = ToolResult.needsConfirmation("即将执行删除操作，请回复\"确认\"以继续。");

        assertFalse(result.isSuccess());
        assertTrue(result.isNeedsConfirmation());
        assertEquals("即将执行删除操作，请回复\"确认\"以继续。", result.getConfirmationMessage());
        assertEquals("即将执行删除操作，请回复\"确认\"以继续。", result.getErrorMessage());
    }

    // ==================== toMap() 转换 ====================

    @Test
    @DisplayName("toMap() 成功结果应包含 success=true 和 data")
    void shouldConvertSuccessToMap() {
        Map<String, Object> data = Map.of("id", 42, "name", "测试");
        ToolResult result = ToolResult.success(data);

        Map<String, Object> map = result.toMap();

        assertEquals(true, map.get("success"));
        assertEquals(data, map.get("data"));
        assertNull(map.get("error"));
    }

    @Test
    @DisplayName("toMap() 成功结果 data 为 null 时应回退为默认消息")
    void shouldFallbackSuccessMapWhenDataIsNull() {
        ToolResult result = ToolResult.success();

        Map<String, Object> map = result.toMap();

        assertEquals(true, map.get("success"));
        assertEquals("操作成功", map.get("data"));
    }

    @Test
    @DisplayName("toMap() 失败结果应包含 success=false 和 error")
    void shouldConvertErrorToMap() {
        ToolResult result = ToolResult.error("查询不到该设备");

        Map<String, Object> map = result.toMap();

        assertEquals(false, map.get("success"));
        assertEquals("查询不到该设备", map.get("error"));
    }

    @Test
    @DisplayName("toMap() 需要确认的结果应包含 needsConfirmation=true 和 message")
    void shouldConvertConfirmationToMap() {
        ToolResult result = ToolResult.needsConfirmation("确定要删除吗？");

        Map<String, Object> map = result.toMap();

        assertEquals(false, map.get("success"));
        assertEquals(true, map.get("needsConfirmation"));
        assertEquals("确定要删除吗？", map.get("message"));
    }

    // ==================== Builder 测试 ====================

    @Test
    @DisplayName("Builder 默认值：needsConfirmation 应为 false")
    void shouldDefaultNeedsConfirmationToFalse() {
        ToolResult result = ToolResult.builder()
                .success(true)
                .data("test")
                .build();

        assertFalse(result.isNeedsConfirmation());
    }

    @Test
    @DisplayName("Builder 构建完整对象应保持所有字段")
    void shouldBuildCompleteObject() {
        ToolResult result = ToolResult.builder()
                .success(false)
                .errorMessage("自定义错误")
                .needsConfirmation(true)
                .confirmationMessage("请确认")
                .data("部分数据")
                .build();

        assertFalse(result.isSuccess());
        assertEquals("自定义错误", result.getErrorMessage());
        assertTrue(result.isNeedsConfirmation());
        assertEquals("请确认", result.getConfirmationMessage());
        assertEquals("部分数据", result.getData());
    }
}
