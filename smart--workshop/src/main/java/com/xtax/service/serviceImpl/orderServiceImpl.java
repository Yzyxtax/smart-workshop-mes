package com.xtax.service.serviceImpl;

import com.xtax.mapper.orderMapper;
import com.xtax.pojo.ProductionOrder;
import com.xtax.service.orderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class orderServiceImpl implements orderService {
    @Autowired
    private orderMapper orderMapper;

    @Override
    public List<ProductionOrder> getOrderByPlan(String planNo) {
        return orderMapper.getOrderByPlan(planNo);
    }

    @Override
    public Object getAllOrder() {
        return orderMapper.getAllOrder();
    }
}
