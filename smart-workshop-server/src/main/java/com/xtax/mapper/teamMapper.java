package com.xtax.mapper;

import com.xtax.vo.MatrixDataVO;
import com.xtax.entity.TeamItem;
import com.xtax.entity.WorkTeam;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface teamMapper {
    // 查询所有班组信息
    @Select("SELECT team_no,team_name from work_teams")
    List<TeamItem> getAllTeam();

    // 根据编号查询班组信息
    WorkTeam getTeamByNo(String teamCode);

    // 添加班组信息
    @Insert("INSERT INTO work_teams(team_no, team_name, team_location, line_no) VALUES(#{teamNo},#{teamName},#{teamLocation},#{lineNo})")
    void addTeam(WorkTeam team);

    //添加班组成员信息
    void setTeamMember(WorkTeam team);

    // 修改班组信息
    void updateTeam(WorkTeam team, String teamCode);

    // 删除班组成员信息
    void deleteTeamMember(String teamNo);

    // 删除班组信息
    @Delete("DELETE FROM work_teams WHERE team_no = #{teamCode}")
    void deleteTeam(String teamCode);

    // 查询班组的成员信息
    @Select("SELECT username FROM users WHERE team_no = #{teamCode}")
    List<String> getTeamMember(String teamCode);

    // 查询矩阵数据
    List<MatrixDataVO> getMatrixData(String teamCode);

    // 保存矩阵数据
    void saveMatrixData(@Param("username") String username, @Param("processName") String processName, @Param("teamCode") String teamCode);

    // 删除矩阵数据
    @Update("update skills set choose=false where team_no=#{teamCode}")
    void deleteMatrixData(String teamCode);

    // 获取班组的班长名称
    @Select("SELECT username FROM users WHERE team_no = #{teamCode} AND position = '班组长'")
    String getLeaderName(String teamCode);

    //为技能表添加班组数据
    void setTeamMemberSkill(WorkTeam team);

    //为技能表清空班组数据
    void deleteTeamMemberSkill(String teamNo);
}
