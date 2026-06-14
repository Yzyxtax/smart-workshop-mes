package com.xtax.ai.tool.bom;

import com.xtax.ai.agent.ToolContext;
import com.xtax.ai.agent.ToolResult;
import com.xtax.entity.Bom;
import com.xtax.service.bomService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * CreateBomTool 单元测试。
 * 验证 BOM 创建的参数校验和默认值逻辑。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@DisplayName("CreateBomTool 创建 BOM 测试")
@ExtendWith(MockitoExtension.class)
class CreateBomToolTest {

    @Mock
    private bomService bomService;

    @InjectMocks
    private CreateBomTool tool;

    private final ToolContext ctx = ToolContext.builder()
            .userId(1001).sessionId(1L).messageId(10L).build();

    @Nested
    @DisplayName("必填参数校验")
    class RequiredParamValidation {

        @Test
        @DisplayName("缺少 bomDrawingNo 应返回错误")
        void shouldFailForMissingDrawingNo() {
            ToolResult result = tool.execute(Map.of("bomNameSpec", "螺栓 M8"), ctx);
            assertFalse(result.isSuccess());
            assertThat(result.getErrorMessage()).contains("缺少必填参数");
        }

        @Test
        @DisplayName("缺少 bomNameSpec 应返回错误")
        void shouldFailForMissingNameSpec() {
            ToolResult result = tool.execute(Map.of("bomDrawingNo", "DWG-001"), ctx);
            assertFalse(result.isSuccess());
            assertThat(result.getErrorMessage()).contains("缺少必填参数");
        }

        @Test
        @DisplayName("空字符串 bomDrawingNo 应视为缺失")
        void shouldFailForEmptyDrawingNo() {
            ToolResult result = tool.execute(Map.of(
                    "bomDrawingNo", "", "bomNameSpec", "螺栓"), ctx);
            assertFalse(result.isSuccess());
        }
    }

    @Nested
    @DisplayName("默认值")
    class DefaultValues {

        @Test
        @DisplayName("未指定 bomQuantity 时应默认为 1")
        void shouldDefaultQuantityToOne() {
            when(bomService.addBom(any(Bom.class))).thenAnswer(inv -> {
                Bom b = inv.getArgument(0);
                b.setId(1);
                return 1;
            });

            tool.execute(Map.of("bomDrawingNo", "DWG-001",
                    "bomNameSpec", "螺栓 M8"), ctx);

            ArgumentCaptor<Bom> captor = ArgumentCaptor.forClass(Bom.class);
            verify(bomService).addBom(captor.capture());
            assertEquals(1, captor.getValue().getQuantity());
        }

        @Test
        @DisplayName("未指定 bomType 时应默认为\"原材料\"")
        void shouldDefaultTypeToRawMaterial() {
            when(bomService.addBom(any(Bom.class))).thenAnswer(inv -> {
                Bom b = inv.getArgument(0);
                b.setId(1);
                return 1;
            });

            tool.execute(Map.of("bomDrawingNo", "DWG-001",
                    "bomNameSpec", "螺栓 M8"), ctx);

            ArgumentCaptor<Bom> captor = ArgumentCaptor.forClass(Bom.class);
            verify(bomService).addBom(captor.capture());
            assertEquals("原材料", captor.getValue().getType());
        }
    }

    @Nested
    @DisplayName("成功创建")
    class SuccessfulCreation {

        @Test
        @DisplayName("完整参数应正确赋值到 Bom 实体")
        void shouldSetAllFieldsCorrectly() {
            when(bomService.addBom(any(Bom.class))).thenAnswer(inv -> {
                Bom b = inv.getArgument(0);
                b.setId(42);
                return 1;
            });

            ToolResult result = tool.execute(Map.of(
                    "bomDrawingNo", "DWG-100",
                    "bomNameSpec", "法兰盘 Φ200",
                    "bomMaterial", "304不锈钢",
                    "bomQuantity", 5,
                    "bomType", "半成品"
            ), ctx);

            assertTrue(result.isSuccess());

            ArgumentCaptor<Bom> captor = ArgumentCaptor.forClass(Bom.class);
            verify(bomService).addBom(captor.capture());
            Bom bom = captor.getValue();
            assertEquals("DWG-100", bom.getDrawingNo());
            assertEquals("法兰盘 Φ200", bom.getNameSpecification());
            assertEquals("304不锈钢", bom.getMaterial());
            assertEquals(5, bom.getQuantity());
            assertEquals("半成品", bom.getType());
        }

        @Test
        @DisplayName("创建成功应返回 ID 和确认消息")
        void shouldReturnIdOnSuccess() {
            when(bomService.addBom(any(Bom.class))).thenAnswer(inv -> {
                Bom b = inv.getArgument(0);
                b.setId(99);
                return 1;
            });

            ToolResult result = tool.execute(Map.of(
                    "bomDrawingNo", "DWG-001",
                    "bomNameSpec", "测试件"
            ), ctx);

            assertTrue(result.isSuccess());
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) result.getData();
            assertThat(data).containsEntry("bomId", 99);
        }

        @Test
        @DisplayName("插入返回 0 时应返回错误")
        void shouldFailWhenInsertReturnsZero() {
            when(bomService.addBom(any(Bom.class))).thenReturn(0);

            ToolResult result = tool.execute(Map.of(
                    "bomDrawingNo", "DWG-001",
                    "bomNameSpec", "测试件"
            ), ctx);

            assertFalse(result.isSuccess());
        }
    }
}
