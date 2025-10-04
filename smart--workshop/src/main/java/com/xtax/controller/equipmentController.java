package com.xtax.controller;

import com.xtax.pojo.Equipment;
import com.xtax.pojo.EquipmentQueryParam;
import com.xtax.pojo.Result;
import com.xtax.pojo.ResultPage;
import com.xtax.service.serviceImpl.equipmentServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/equipment")
public class equipmentController {
    @Autowired
    private equipmentServiceImpl equipmentServiceImpl;

    @GetMapping
    public Result getAllEquipment(EquipmentQueryParam equipmentQueryParam){
        Integer page = equipmentQueryParam.getPage();
        Integer pageSize = equipmentQueryParam.getPageSize();
        String name = equipmentQueryParam.getName();
        String type = equipmentQueryParam.getType();
        String model = equipmentQueryParam.getModel();

        log.info("条件分页查询：{}，{}，{}，{}，{}", page, pageSize, name, type, model);
        ResultPage<Equipment> equipment = equipmentServiceImpl.getAllEquipment(equipmentQueryParam);
        if(equipment != null) return Result.success(equipment);
        return Result.error("查询失败");
    }

    @PostMapping
    public Result addEquipment(@RequestBody Equipment equipment){
        log.info("添加设备：{}", equipment);
        int add = equipmentServiceImpl.addEquipment(equipment);
        if(add > 0) return Result.success("添加成功");
        return Result.error("添加失败");
    }

    @GetMapping("/{id}")
    public Result getEquipmentById(@PathVariable Integer id){
        log.info("查询id为{}的设备",id);
        Equipment equipment = equipmentServiceImpl.getEquipmentById(id);
        if(equipment != null) return Result.success(equipment);
        return Result.error("查询失败");
    }
}
