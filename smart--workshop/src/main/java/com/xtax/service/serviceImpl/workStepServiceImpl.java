package com.xtax.service.serviceImpl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xtax.mapper.workStepMapper;
import com.xtax.pojo.ResultPage;
import com.xtax.pojo.WorkStep;
import com.xtax.pojo.WorkStepQueryParam;
import com.xtax.service.workStepService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class workStepServiceImpl implements workStepService {
    @Autowired
    private workStepMapper workStepMapper;
    //查询所有工步信息
    @Override
    public ResultPage<WorkStep> getWorkStep(WorkStepQueryParam workStepQueryParam) {
        Integer page = workStepQueryParam.getPage();
        Integer pageSize = workStepQueryParam.getPageSize();
        String stepName = workStepQueryParam.getStepName();
        String equipmentName = workStepQueryParam.getEquipmentName();
        String processName = workStepQueryParam.getProcessName();

        PageHelper.startPage(page, pageSize);

        List<WorkStep> list = workStepMapper.getWorkStep(stepName, equipmentName, processName);
        Page<WorkStep> p = (Page<WorkStep>) list;
        return new ResultPage<WorkStep>(p.getTotal(), p.getResult());
    }

    //添加工步信息
    @Override
    public int addWorkStep(WorkStep workStep) {
        return workStepMapper.addWorkStep(workStep);
    }

    //修改工步信息
    @Override
    public int updateWorkStep(WorkStep workStep) {
        return workStepMapper.updateWorkStep(workStep);
    }

    //删除工步信息
    @Override
    public int deleteWorkStep(List<Integer> stepIds) {
        return workStepMapper.deleteWorkStep(stepIds);
    }

    //查询所有工步信息
    @Override
    public List<WorkStep> getAllWorkStep() {
        return workStepMapper.getAllWorkStep();
    }
}
