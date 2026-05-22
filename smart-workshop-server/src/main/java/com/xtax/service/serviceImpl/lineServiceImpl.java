package com.xtax.service.serviceImpl;

import com.xtax.mapper.lineMapper;
import com.xtax.entity.LineCompose;
import com.xtax.vo.LineComposeVO;
import com.xtax.entity.ProductionLine;
import com.xtax.entity.TeamSkillInfo;
import com.xtax.service.lineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class lineServiceImpl implements lineService {
    @Autowired
    private lineMapper lineMapper;

    // 获取所有产线信息
    @Override
    public List<ProductionLine> getAllLine() {
        return lineMapper.getAllLine();
    }

    // 添加产线信息
    @Override
    public void addLine(ProductionLine productionLine) {
        lineMapper.addLine(productionLine);
    }

    //修改产线信息
    @Override
    public void updateLine(ProductionLine productionLine, String lineCode) {
        lineMapper.updateLine(productionLine, lineCode);
    }

    // 删除产线信息
    @Override
    public void deleteLine(String lineNo) {
        lineMapper.deleteLine(lineNo);
    }

    // 获取产线班组信息
    @Transactional(rollbackFor = Exception.class)
    @Override
    public LineCompose getLineCompose(String lineNo) {
        // 获取已分配的班组
        List<TeamSkillInfo> assignedTeams = lineMapper.getAssignedTeamsWithSkills(lineNo);

        // 获取未分配的班组
        List<TeamSkillInfo> unassignedTeams = lineMapper.getUnassignedTeamsWithSkills();

        // 组装结果
        List<LineCompose.compose> allComposes = new ArrayList<>();

        // 添加已分配的班组
        for (TeamSkillInfo team : assignedTeams) {
            LineCompose.compose compose = new LineCompose.compose();
            compose.setTeamNo(team.getTeamNo());
            compose.setTeamName(team.getTeamName());
            compose.setType("assigned");
            compose.setProcessMatrix(team.getProcessMatrix() != null ? team.getProcessMatrix() : new HashMap<>());
            allComposes.add(compose);
        }

        // 添加未分配的班组
        for (TeamSkillInfo team : unassignedTeams) {
            LineCompose.compose compose = new LineCompose.compose();
            compose.setTeamNo(team.getTeamNo());
            compose.setTeamName(team.getTeamName());
            compose.setType("unassigned");
            compose.setProcessMatrix(team.getProcessMatrix() != null ? team.getProcessMatrix() : new HashMap<>());
            allComposes.add(compose);
        }

        LineCompose result = new LineCompose();
        result.setLineNo(lineNo);
        result.setComposes(allComposes);

        return result;
    }

    // 保存产线班组信息
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLineCompose(LineComposeVO lineCompose) {
        //先根据产线编号删除该产线的所有班组信息
        lineMapper.deleteLineTeam(lineCompose.getLineNo());

        //添加新的班组信息
        for (String teamNo : lineCompose.getTeams()){
            lineMapper.addLineTeam(lineCompose.getLineNo(), teamNo);
        }
    }

    // 获取产线信息
    @Override
    public ProductionLine getLine(String lineNo) {
        return lineMapper.getLine(lineNo);
    }
}
