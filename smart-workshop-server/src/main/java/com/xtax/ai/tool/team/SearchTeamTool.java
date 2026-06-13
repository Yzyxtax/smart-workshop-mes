package com.xtax.ai.tool.team;

import com.xtax.ai.agent.ToolContext;
import com.xtax.ai.agent.ToolHandler;
import com.xtax.ai.agent.ToolResult;
import com.xtax.ai.annotation.AiTool;
import com.xtax.ai.annotation.ToolParam;
import com.xtax.entity.TeamItem;
import com.xtax.service.teamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 搜索班组工具。
 * 按关键词搜索班组信息。
 *
 * @author AI Module Design
 * @since 2026-06-13
 */
@AiTool(
    name = "search_team",
    description = "按名称或编号模糊搜索班组。返回匹配的班组列表（含所属产线和人员信息）。",
    category = "班组管理",
    label = "搜索班组"
)
@Component
@RequiredArgsConstructor
public class SearchTeamTool implements ToolHandler {

    @ToolParam(description = "搜索关键词（班组名称或编号）", required = true)
    private String keyword;

    private final teamService teamService;

    @Override
    public ToolResult execute(Map<String, Object> params, ToolContext ctx) {
        String keyword = (String) params.get("keyword");

        List<TeamItem> allTeams = teamService.getAllTeam();
        if (allTeams == null || allTeams.isEmpty()) {
            return ToolResult.success(Map.of("count", 0, "teams", List.of()));
        }

        // 按关键词过滤
        List<TeamItem> filtered = allTeams.stream()
                .filter(t -> {
                    String name = t.getTeamName() != null ? t.getTeamName() : "";
                    String no = t.getTeamNo() != null ? t.getTeamNo() : "";
                    return name.contains(keyword) || no.contains(keyword);
                })
                .collect(Collectors.toList());

        return ToolResult.success(Map.of(
                "count", filtered.size(),
                "teams", filtered
        ));
    }
}
