package com.xtax.controller;

import com.xtax.vo.Result;
import com.xtax.service.serviceImpl.workOrderServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/workOrder")
public class workOrderController {
    @Autowired
    private workOrderServiceImpl workOrderServiceImpl;

    @GetMapping("/{orderNo}")
    public Result getWorkOrderByOrder (@PathVariable String orderNo){
        log.info("查询订单{}所包含的工单信息", orderNo);
        return Result.success(workOrderServiceImpl.getWorkOrderByOrder(orderNo));
    }
}
