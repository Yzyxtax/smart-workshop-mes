package com.xtax.service;

import com.xtax.pojo.MatrixDataDTO;
import com.xtax.pojo.MatrixDataVO;
import com.xtax.pojo.TeamItem;
import com.xtax.pojo.WorkTeam;

import java.util.List;

public interface teamService {
    // 查询所有班组信息
    List<TeamItem> getAllTeam();

    // 根据班组编号查询班组信息
    WorkTeam getTeamByNo(String teamCode);

    // 添加班组信息
    void addTeam(WorkTeam team);

    // 修改班组信息
    void updateTeam(WorkTeam team, String teamCode);

    // 删除班组信息
    void deleteTeam(String teamCode);

    // 查询班组成员技能矩阵信息
    List<MatrixDataVO> getMatrixData(String teamCode);

    // 保存班组技能矩阵信息
    void saveMatrixData(MatrixDataDTO matrixDataDTO);
}
