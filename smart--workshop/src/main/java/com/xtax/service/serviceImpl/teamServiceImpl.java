package com.xtax.service.serviceImpl;

import com.xtax.mapper.teamMapper;
import com.xtax.pojo.MatrixDataDTO;
import com.xtax.pojo.MatrixDataVO;
import com.xtax.pojo.TeamItem;
import com.xtax.pojo.WorkTeam;
import com.xtax.service.teamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class teamServiceImpl implements teamService {
    @Autowired
    private teamMapper teamMapper;

    // 查询所有班组信息
    @Override
    public List<TeamItem> getAllTeam() {
        return teamMapper.getAllTeam();
    }

    // 根据班组编号查询班组信息
    @Transactional(rollbackFor = Exception.class)
    @Override
    public WorkTeam getTeamByNo(String teamCode) {
        WorkTeam team = teamMapper.getTeamByNo(teamCode);
        team.setUserName(teamMapper.getTeamMember(teamCode));
        return team;
    }

    // 添加班组信息
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addTeam(WorkTeam team) {
        teamMapper.addTeam(team);
        if(!team.getTeamLeader().isEmpty() || !team.getUserName().isEmpty()) {
            teamMapper.setTeamMember(team);
        }
    }

    // 修改班组信息
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateTeam(WorkTeam team, String teamCode) {
        // 修改班组信息
        teamMapper.updateTeam(team, teamCode);
        // 修改班组成员信息
        teamMapper.deleteTeamMember(team.getTeamNo());
        teamMapper.setTeamMember(team);
    }

    // 删除班组信息
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteTeam(String teamCode) {
        //先删除班组成员信息，再删除班组信息
        teamMapper.deleteTeamMember(teamCode);
        teamMapper.deleteTeam(teamCode);
    }

    // 查询班组矩阵数据
    @Override
    public List<MatrixDataVO> getMatrixData(String teamCode ) {
        return teamMapper.getMatrixData(teamCode);
    }

    // 保存班组矩阵数据
    @Override
    public void saveMatrixData(MatrixDataDTO matrixDataDTO) {
        //先清空旧数据
        teamMapper.deleteMatrixData(matrixDataDTO.getTeamCode());
        List<MatrixDataDTO.MatrixData> matrixData = matrixDataDTO.getMatrixData();
        for (MatrixDataDTO.MatrixData data : matrixData) {
            teamMapper.saveMatrixData(data.getUsername(), data.getProcessName(), matrixDataDTO.getTeamCode());
        }
    }
}
