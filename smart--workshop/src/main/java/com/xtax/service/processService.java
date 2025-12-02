package com.xtax.service;

import com.xtax.pojo.ProcessQueryParam;
import com.xtax.pojo.Processes;
import com.xtax.pojo.ResultPage;

import java.util.List;

public interface processService {
    // 查询工序信息
    ResultPage<Processes> getProcess(ProcessQueryParam processQueryParam);

    // 添加工序信息
    boolean addProcess(Processes processes);

    // 删除工序信息
    boolean deleteProcess(List<Integer> ids);

    // 修改工序信息
    boolean updateProcess(Processes processes);

    // 查询所有工序信息
    List<Processes> listAll();
}
