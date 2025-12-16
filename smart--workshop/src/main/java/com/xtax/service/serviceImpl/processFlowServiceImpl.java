package com.xtax.service.serviceImpl;

import com.xtax.mapper.processFlowMapper;
import com.xtax.pojo.EdgeData;
import com.xtax.pojo.FlowChartData;
import com.xtax.pojo.FlowChartDataDTO;
import com.xtax.pojo.ProcessFlow;
import com.xtax.service.processFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class processFlowServiceImpl implements processFlowService {
    @Autowired
    private processFlowMapper processFlowMapper;

    //查询所有工艺流程信息
    @Override
    public List<ProcessFlow> getAllFlow() {
        return processFlowMapper.getAllFlow();
    }

    //查询指定工艺流程的流程图信息
    @Override
    public FlowChartData getProcessFlowChart(Integer id) {
        //1. 新建流程图数据对象
        FlowChartData flowChartData = new FlowChartData();
        //2. 获取所有工序信息
        List<HashMap<String, Object>> processList = processFlowMapper.getProcessNameByFlowId(id);
        HashMap<Integer, String> processData = new HashMap<>();
        for (Map<String, Object> map : processList) {
            Integer processId = ((Number) map.get("id")).intValue();
            String processName = (String) map.get("process_name");
            processData.put(processId, processName);
        }
        //3. 获取原始的边信息
        List<HashMap<String,Integer>> processOrderList = processFlowMapper.getProcessOrderByFlowId(id);

        //4. 对边信息进行转换
        HashMap<String,List<String>> edgeData = new HashMap<>();
        // 将工序ID关系转换为工序名称关系
        for (Map<String, Integer> map : processOrderList) {
            Integer currentProcessId = map.get("process_id");
            Integer previousProcessId = map.get("previous_process_id");

            // 如果前序工序ID为null，说明这是起始节点，不需要添加边
            if (previousProcessId == null) {
                continue;
            }

            String currentProcessName = processData.get(currentProcessId);
            String previousProcessName = processData.get(previousProcessId);

            // 如果都能找到对应的工序名称，则添加到边数据中
            if (currentProcessName != null && previousProcessName != null) {
                // 如果key不存在，创建新的列表
                edgeData.computeIfAbsent(previousProcessName, k -> new ArrayList<>()).add(currentProcessName);
            }
        }

        // 设置节点数据（工序名称列表）
        List<String> nodeData = new ArrayList<>(processData.values());

        flowChartData.setFlowId(id);
        flowChartData.setNodeData(nodeData);
        flowChartData.setEdgeData(edgeData);

        return flowChartData;
    }

    //保存流程图信息
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveProcessFlowChart(FlowChartDataDTO flowChartDataDTO) {
        //先删除所有数据
        processFlowMapper.deleteProcessSequence(flowChartDataDTO.getFlowId());
        processFlowMapper.deleteProcessFlowProcesses(flowChartDataDTO.getFlowId());

        Integer flowId = flowChartDataDTO.getFlowId();
        List<String> nodeData = flowChartDataDTO.getNodeData();
        List<EdgeData> edgeData = flowChartDataDTO.getEdgeData();

        //1. 处理节点数据，转化为工序id列表
        List<Integer> processIdList = new ArrayList<>();
        for (String nodeName : nodeData) {
            // 根据工序名称查找对应的工序id
            Integer processId = processFlowMapper.getProcessIdByName(nodeName);
            processIdList.add(processId);
        }
        //2. 将工序名称和工序id包装成一个map，方便后续处理
        Map<String, Integer> processNameIdMap = new HashMap<>();
        for (int i = 0; i < nodeData.size(); i++) {
            processNameIdMap.put(nodeData.get(i), processIdList.get(i));
        }
        //3. 处理边数据
        for (EdgeData edge : edgeData) {
            String sourceNodeName = edge.getSourceNodeName();
            String targetNodeName = edge.getTargetNodeName();
            // 根据工序名称查找对应的工序id
            Integer sourceProcessId = processNameIdMap.get(sourceNodeName);
            Integer targetProcessId = processNameIdMap.get(targetNodeName);
            processFlowMapper.insertProcessSequence(targetProcessId, sourceProcessId, flowId);
        }
        // 4. 保存工序信息
        processFlowMapper.insertProcessFlowProcesses(flowId, processIdList);
    }

    //添加工艺流程信息
    @Override
    public void addProcessFlow(ProcessFlow processFlow) {
        processFlowMapper.addProcessFlow(processFlow);
    }

    //修改工序信息
    @Override
    public void updateProcessFlow(ProcessFlow processFlow) {
        processFlowMapper.updateProcessFlow(processFlow);
    }

    //删除工序信息
    @Override
    public void deleteProcessFlow(Integer id) {
        processFlowMapper.deleteProcessFlow(id);
    }
}
