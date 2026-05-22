package com.xtax.service.serviceImpl;

import com.xtax.mapper.workOrderMapper;
import com.xtax.service.workOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class workOrderServiceImpl implements workOrderService {
    @Autowired
    private workOrderMapper workOrderMapper;

    // 根据订单编号查询工单
    @Override
    public Object getWorkOrderByOrder(String orderNo) {
        return workOrderMapper.getWorkOrderByOrder(orderNo);
    }
}
