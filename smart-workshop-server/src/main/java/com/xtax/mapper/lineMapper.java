package com.xtax.mapper;

import com.xtax.entity.ProductionLine;
import com.xtax.entity.TeamSkillInfo;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface lineMapper {
    //获取所有产线信息
    List<ProductionLine> getAllLine();

    //添加产线信息
    void addLine(ProductionLine productionLine);

    //更新产线信息
    void updateLine(@Param("productionLine") ProductionLine productionLine,@Param("lineCode") String lineCode);

    //删除产线信息
    @Delete("delete from production_lines where line_no=#{lineNo}")
    void deleteLine(String lineNo);

    //获取已分配给产线的班组及其工序技能
    List<TeamSkillInfo> getAssignedTeamsWithSkills(String lineNo);

    //获取未分配到任何产线的班组及其工序技能
    List<TeamSkillInfo> getUnassignedTeamsWithSkills();

    //删除产线班组信息
    @Update("update work_teams set line_no = null where line_no = #{lineNo}")
    void deleteLineTeam(String lineNo);

    //保存产线班组信息
    @Update("update work_teams set line_no = #{lineNo} where team_no = #{teamNo}")
    void addLineTeam(@Param("lineNo") String lineNo, @Param("teamNo") String teamNo);

    //判断工艺流程编号的产线是否存在
    @Select("SELECT EXISTS(SELECT 1 FROM production_lines WHERE process_flow_id = #{processFlowId} AND line_status = '空闲')")
    boolean isLineExist(@Param("processFlowId") Integer processFlowId);

    //根据工艺流程编号获取空闲产线编号（用于计划发布时自动选择产线）
    @Select("SELECT line_no FROM production_lines WHERE process_flow_id = #{processFlowId} AND line_status = '空闲' ORDER BY line_no LIMIT 1")
    String getAvailableLineNoByFlowId(@Param("processFlowId") Integer processFlowId);

    //获取指定产线的信息
    @Select("SELECT * FROM production_lines WHERE line_no = #{lineNo}")
    ProductionLine getLine(String lineNo);
}
