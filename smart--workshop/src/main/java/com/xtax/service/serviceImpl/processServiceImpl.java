package com.xtax.service.serviceImpl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xtax.mapper.processMapper;
import com.xtax.pojo.ProcessQueryParam;
import com.xtax.pojo.Processes;
import com.xtax.pojo.ResultPage;
import com.xtax.service.processService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class processServiceImpl implements processService {
    @Autowired
    private processMapper processMapper;

    //查询所有工序信息
    @Override
    public ResultPage<Processes> getProcess(ProcessQueryParam processQueryParam) {
        Integer page = processQueryParam.getPage();
        Integer pageSize = processQueryParam.getPageSize();
        String processName = processQueryParam.getProcessName();
        List<Integer> inputBom = processQueryParam.getInputBom();
        List<Integer> outputBom = processQueryParam.getOutputBom();

        PageHelper.startPage(page, pageSize);

        List<Processes> list = processMapper.getProcess(processName, inputBom, outputBom);
        Page<Processes> p = (Page<Processes>) list;
        return new ResultPage<Processes>(p.getTotal(), p.getResult());
    }

    //添加工序信息
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addProcess(Processes processes) {
        // 1. 插入主表，并自动回填 ID
        processMapper.addProcess(processes);

        Integer processId = processes.getId();

        // 2. 插入 input BOM
        if (!processes.getInputBomId().isEmpty()) {
            processMapper.insertInputBom(processId, processes.getInputBomId());
        }

        // 3. 插入 output BOM
        if (!processes.getOutputBomId().isEmpty()) {
            processMapper.insertOutputBom(processId, processes.getOutputBomId());
        }

        // 4. 插入工步
        if (!processes.getWorkStepId().isEmpty()) {
            processMapper.insertWorkStep(processId, processes.getWorkStepId());
        }

        return true;
    }

    //删除工序信息
    @Override
    public boolean deleteProcess(List<Integer> ids) {
        int i = processMapper.deleteProcess(ids);
        return i>0;
    }

    //修改工序信息
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateProcess(Processes processes) {
        int i = processMapper.updateProcess(processes);
        //删除旧的信息
        processMapper.deleteInputBom(processes.getId());
        processMapper.deleteOutputBom(processes.getId());
        processMapper.deleteWorkStep(processes.getId());

        //插入新的信息
        processMapper.insertInputBom(processes.getId(), processes.getInputBomId());
        processMapper.insertOutputBom(processes.getId(), processes.getOutputBomId());
        processMapper.insertWorkStep(processes.getId(), processes.getWorkStepId());
        return i > 0;
    }

    //查询所有工序信息
    @Override
    public List<Processes> listAll() {
        return processMapper.getProcess(null, null, null);
    }
}
