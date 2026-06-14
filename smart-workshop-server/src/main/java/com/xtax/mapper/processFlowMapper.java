package com.xtax.mapper;

import com.xtax.entity.ProcessFlow;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;

@Mapper
public interface processFlowMapper {
    //查询所有工序信息
    @Select("select * from process_flows order by bom_id")
    List<ProcessFlow> getAllFlow();

    //查询工艺流程包含的工序名称
    List<HashMap<String, Object>> getProcessNameByFlowId(Integer flowId);

    //查询工艺流程包含的工序顺序信息
    List<HashMap<String,Integer>> getProcessOrderByFlowId(Integer flowId);

    //删除工序顺序
    void deleteProcessSequence(Integer processFlowId);

    //删除工序信息
    void deleteProcessFlowProcesses(Integer processFlowId);

    //根据工序名称查询工序ID
    @Select("select id from processes where process_name=#{nodeName}")
    Integer getProcessIdByName(String nodeName);

    //添加工序顺序
    @Insert("insert into process_sequences(process_id, previous_process_id, process_flow_id) value(#{processId}, #{previousProcessId}, #{processFlowId})")
    void insertProcessSequence(Integer processId, Integer previousProcessId, Integer processFlowId);

    //添加工序信息
    void insertProcessFlowProcesses(Integer flowId, List<Integer> processIdList);

    //添加工序信息
    void addProcessFlow(ProcessFlow processFlow);

    //修改工序信息
    void updateProcessFlow(ProcessFlow processFlow);

    //删除工序信息
    void deleteProcessFlow(Integer id);

    //判断该BOM编号的工艺流程是否存在
    @Select("SELECT EXISTS(SELECT 1 FROM process_flows WHERE bom_id = #{bomId} AND status = '有效')")
    boolean isFlowExist(@Param("bomId") Integer bomId);

    //根据BOM编号查询所有工艺流程ID
    @Select("select id from process_flows where bom_id= #{bomId} and status='有效'")
    List<Integer> getProcessFlowIdList(Integer bomId);

    /** 查询工艺流程包含的工序ID列表 */
    @Select("SELECT process_id FROM process_flow_processes WHERE process_flow_id = #{flowId}")
    List<Integer> getProcessIdsByFlowId(Integer flowId);
}
